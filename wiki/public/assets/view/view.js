// The zoom on the page, as the game draws it: the title screen's panorama seen through a camera whose field of view
// the ported ZoomController sets, the HUD drawn over it at a whole-number GUI scale, and the bars over that.
//
//   <div class="cze-view"></div>                         a view to try: hold C or V, scroll, ↑ ↓, look around
//   <div class="cze-view" data-controls></div>           the same with the settings beside it
//   <div class="cze-view" data-demo="tour"></div>        a scripted run with captions (tour, curves, snaps, profiles, ultrawide)
//
// The panorama comes from Mojang's asset server and the HUD sprites from the game's client jar, both when the page
// opens; the wiki keeps no copies (tools/view-data.py writes data.json, which says where they are).

import { Zoom, defaultConfig, setUltrawide, CURVES, curve, ASPECT_PRESETS, barsForAspect, aspectGeometry } from './zoom.js';

const BASE = new URL('.', import.meta.url);
const GUI_WIDTH = 480;

// ------------------------------------------------------------------ files

/** One file out of the jar: its local header, then its bytes, inflated if they're deflated. */
async function entry(jar, [offset, size, method]) {
  let res;
  for (let tries = 0; ; tries++) {
    // Busy, the server sometimes sends the whole jar instead of the range: stop that and ask again. Each file gets its own
    // URL (the server ignores the query), as the browser lets one request at a time use a URL.
    const stop = new AbortController();
    res = await fetch(`${jar}?at=${offset}`, { cache: 'no-store', signal: stop.signal, headers: { Range: `bytes=${offset}-${offset + 30 + 512 + size}` } });
    if (res.status === 206) break;
    stop.abort();
    if (tries >= 3) throw new Error(`range request answered ${res.status}`);
    await new Promise(done => setTimeout(done, 250 * (tries + 1)));
  }
  const buf = new Uint8Array(await res.arrayBuffer()), view = new DataView(buf.buffer);
  const start = 30 + view.getUint16(26, true) + view.getUint16(28, true), data = buf.subarray(start, start + size);
  if (method === 0) return data;
  const stream = new Blob([data]).stream().pipeThrough(new DecompressionStream('deflate-raw'));
  return new Uint8Array(await new Response(stream).arrayBuffer());
}

let shared;
/** data.json, the panorama's six URLs and the sprites, once for every view on the page. */
function load() {
  shared ??= (async () => {
    const data = await fetch(new URL('data.json', BASE)).then(r => r.json());
    const sprites = new Map();
    const faces = fetch(data.index).then(r => r.json()).then(({ objects }) => data.panorama.map(p => {
      const h = objects[p].hash;
      return `https://resources.download.minecraft.net/${h.slice(0, 2)}/${h}`;
    }));
    const queue = Object.entries(data.entries);
    const pull = async ([path, at]) => {
      const key = path.replace(/^assets\/minecraft\/textures\/(gui\/sprites\/)?/, '').replace(/\.png$/, '');
      sprites.set(key, await createImageBitmap(new Blob([await entry(data.jar, at)], { type: 'image/png' })));
    };
    const pulled = Promise.all(Array.from({ length: 6 }, async () => {
      while (queue.length) await pull(queue.shift()).catch(e => console.warn('[cze-view] a sprite did not load', e));
    }));
    return { faces: await faces, sprites, pulled };
  })();
  return shared;
}

// ------------------------------------------------------------------ views

const views = [];

async function mount(root) {
  const script = root.dataset.demo ? DEMOS[root.dataset.demo] : null;
  if (root.dataset.demo && !script) throw new Error('no demo named ' + root.dataset.demo);
  root.classList.toggle('is-demo', !!script);
  root.innerHTML = `<div class="cze-view__stage"><div class="cze-view__world"><div class="cze-view__cam"></div></div>
    <canvas class="cze-view__hud"></canvas><span class="cze-view__keys" aria-hidden="true"></span>
    <span class="cze-view__hint" aria-hidden="true"></span></div>
    <div class="cze-view__bar"></div>`;
  const stage = root.querySelector('.cze-view__stage'), world = root.querySelector('.cze-view__world');
  const cam = root.querySelector('.cze-view__cam'), canvas = root.querySelector('canvas'), keys = root.querySelector('.cze-view__keys');
  const bar = root.querySelector('.cze-view__bar');
  const { faces, sprites } = await load();

  // The panorama: six faces of a cube around the camera, in the order the game's CubeMap reads them.
  const FACE = ['', 'rotateY(-90deg)', 'rotateY(180deg)', 'rotateY(90deg)', 'rotateX(-90deg)', 'rotateX(90deg)'];
  const S = 1024;
  faces.forEach((url, i) => {
    const img = document.createElement('img');
    img.src = url;
    img.alt = '';
    img.draggable = false;
    img.style.transform = `${FACE[i]} translateZ(${-S / 2}px) scale(1.002)`;
    cam.append(img);
  });

  let cfg, zoom, mode = 'NONE', held = null, previewFraming = false, yaw = 0, pitch = 0, look = { yaw: 0, pitch: 0 }, demo = null;
  const build = () => {
    cfg = defaultConfig();
    zoom = new Zoom(cfg);
    mode = 'NONE';
    held = null;
    yaw = look.yaw = 25;
    pitch = look.pitch = 4;
  };
  build();

  // Size: 16:9 on the page's 8px grid, the HUD at a whole-number scale like the game's GUI scale.
  let w = 0, h = 0, scale = 1, guiW = 0, guiH = 0;
  let aspectIndex = ASPECT_PRESETS.length - 1;
  let sidePercent = 0, targetSidePercent = 0;
  let refreshAspect = () => {};
  const resize = () => {
    const dpr = window.devicePixelRatio || 1, full = document.fullscreenElement === root;
    w = stage.clientWidth;
    h = full ? root.clientHeight - bar.offsetHeight : Math.max(8, Math.round(w * 9 / 16 / 8) * 8);
    stage.style.height = h + 'px';
    const devW = Math.round(w * dpr);
    scale = Math.max(1, Math.round(devW / GUI_WIDTH), Math.round(dpr));
    canvas.width = devW;
    canvas.height = Math.round(h * dpr);
    guiW = Math.floor(canvas.width / scale);
    guiH = Math.floor(canvas.height / scale);
    if (aspectIndex >= 0) applyAspect(aspectIndex);
    refreshAspect();
  };
  new ResizeObserver(resize).observe(root);
  resize();

  function applyAspect(index) {
    aspectIndex = (index + ASPECT_PRESETS.length) % ASPECT_PRESETS.length;
    const geometry = aspectGeometry(w / h, ASPECT_PRESETS[aspectIndex], cfg.cinematicBarsPercent);
    const percent = geometry.top;
    targetSidePercent = geometry.side;
    // Preserve the configured Legacy height when selecting a ratio.
    zoom.heldBarsPercent = percent;
    if (zoom.activeMode === 'CINEMATIC') {
      zoom.targetBarsPercent = percent;
      if (!cfg.smoothBarChange) zoom.currentBarsPercent = percent;
    }
    refreshAspect();
  }
  function stepAspect(direction) {
    const live = (w / h) / Math.max(.01, 1 - 2 * zoom.heldBarsPercent / 100);
    const closest = ASPECT_PRESETS.reduce((best, preset, i) => Math.abs(preset.ratio - live) < Math.abs(ASPECT_PRESETS[best].ratio - live) ? i : best, 0);
    applyAspect((aspectIndex < 0 ? closest : aspectIndex) + direction);
    showKeys(direction > 0 ? '→' : '←');
  }

  let keysLeft = 0;
  const showKeys = text => {
    keys.textContent = text;
    keysLeft = 1100;
  };

  // Input, for the view to try: C (or the button) holds the cinematic zoom, V the one without bars; the wheel and ↑ ↓
  // while zoomed. A click locks the mouse to the view so it looks around like in game, at the zoom's sensitivity, until
  // Esc; touch drags instead.
  const self = { root };
  views.push(self);
  if (!script) {
    stage.tabIndex = 0;
    stage.setAttribute('role', 'application');
    stage.setAttribute('aria-label', 'Zoom view. Hold C to zoom with bars, V without. Scroll while zoomed. Up and down arrows resize the bars. Left and right arrows cycle framing presets while zoomed. Click to lock the mouse and look around; Escape lets go.');
    const hint = root.querySelector('.cze-view__hint');
    const locked = () => document.pointerLockElement === stage;
    let refused = false;
    const turn = (dx, dy, k) => {
      k *= zoom.sensitivity();
      look.yaw += dx * k;
      look.pitch = Math.max(-89, Math.min(89, look.pitch - dy * k));
    };
    const showHint = () => {
      hint.textContent = locked() ? 'Esc to let go of the mouse' : refused ? 'Drag to look around' : 'Click to look around';
      root.classList.toggle('is-locked', locked());
      hint.classList.add('is-shown');
      clearTimeout(hint.timer);
      if (locked()) hint.timer = setTimeout(() => hint.classList.remove('is-shown'), 2000);
    };
    showHint();
    document.addEventListener('pointerlockerror', () => {
      refused = true;
      hint.textContent = 'Drag to look around';
    });
    document.addEventListener('pointerlockchange', () => {
      showHint();
      if (!locked() && held && !(held === 'REGULAR' ? cfg.regularToggle : cfg.cinematicToggle)) held = null;
    });
    const keyFor = e => (e.key === 'c' || e.key === 'C' ? 'CINEMATIC' : e.key === 'v' || e.key === 'V' ? 'REGULAR' : null);
    stage.addEventListener('keydown', e => {
      const m = keyFor(e);
      if (m) {
        if (e.repeat) return e.preventDefault();
        const toggle = m === 'REGULAR' ? cfg.regularToggle : cfg.cinematicToggle;
        held = toggle && held === m ? null : m;
        e.preventDefault();
      } else if ((e.key === 'ArrowUp' || e.key === 'ArrowDown') && cfg.arrowKeysAdjustBars) {
        if (zoom.adjustBars(e.key === 'ArrowUp' ? cfg.barAdjustStep : -cfg.barAdjustStep)) { aspectIndex = ASPECT_PRESETS.length - 1; cfg.cinematicBarsPercent = zoom.heldBarsPercent; targetSidePercent = 0; refreshAspect(); e.preventDefault(); }
      }
    });
    stage.addEventListener('keydown', e => {
      if ((e.key === 'ArrowLeft' || e.key === 'ArrowRight') && zoom.activeMode === 'CINEMATIC' && cfg.arrowKeysAdjustBars) {
        stepAspect(e.key === 'ArrowRight' ? 1 : -1);
        e.preventDefault();
      }
    });
    stage.addEventListener('keyup', e => {
      const m = keyFor(e);
      if (m && held === m && !(m === 'REGULAR' ? cfg.regularToggle : cfg.cinematicToggle)) held = null;
    });
    stage.addEventListener('blur', () => {
      if (held && !previewFraming && !(held === 'REGULAR' ? cfg.regularToggle : cfg.cinematicToggle)) held = null;
    });
    stage.addEventListener('wheel', e => {
      const v = -Math.sign(e.deltaY) * Math.max(1, Math.round(Math.abs(e.deltaMode === 1 ? e.deltaY / 3 : e.deltaY / 100)));
      if (zoom.wheel(v)) e.preventDefault();
    }, { passive: false });
    let drag = null;
    stage.addEventListener('pointerdown', e => {
      stage.focus({ preventScroll: true });
      // Dragging works too, for touch and for a page where the lock is refused.
      if (e.pointerType === 'mouse' && stage.requestPointerLock && !locked() && !refused) stage.requestPointerLock()?.catch?.(() => {});
      drag = { x: e.clientX, y: e.clientY };
      stage.setPointerCapture(e.pointerId);
    });
    stage.addEventListener('pointermove', e => {
      if (locked()) return turn(e.movementX, e.movementY, 0.15);
      if (!drag) return;
      turn(e.clientX - drag.x, e.clientY - drag.y, 0.25);
      drag = { x: e.clientX, y: e.clientY };
    });
    stage.addEventListener('pointerup', () => (drag = null));

    // One button for each profile, like the two keys: held, or clicked on and off when the profile toggles.
    const toggles = m => (m === 'REGULAR' ? cfg.regularToggle : cfg.cinematicToggle);
    const holds = [['CINEMATIC', 'Zoom with bars (C)'], ['REGULAR', 'Zoom without bars (V)']].map(([m, label]) => {
      const b = button(label);
      b.addEventListener('pointerdown', e => {
        held = toggles(m) && held === m ? null : m;
        b.setPointerCapture(e.pointerId);
      });
      const letGo = () => {
        if (held === m && !toggles(m)) held = null;
      };
      b.addEventListener('pointerup', letGo);
      b.addEventListener('pointercancel', letGo);
      return [m, b];
    });
    self.pressed = () => holds.forEach(([m, b]) => b.setAttribute('aria-pressed', String(held === m)));
    self.note = document.createElement('span');
    self.note.className = 'cze-view__note';
    bar.append(...holds.map(([, b]) => b), self.note);
    const framing = document.createElement('div');
    framing.className = 'cze-view__framing';
    framing.setAttribute('role','group');framing.setAttribute('aria-label','Aspect ratio presets');
    const previous = button('←'), next = button('→');
    previous.setAttribute('aria-label','Previous aspect ratio');next.setAttribute('aria-label','Next aspect ratio');
    const sample=document.createElement('span');sample.className='cze-view__frame-sample';sample.setAttribute('aria-hidden','true');
    const label=document.createElement('output');label.setAttribute('aria-live','polite');
    const preview=button('Preview framing');preview.setAttribute('aria-pressed','false');
    
    preview.addEventListener('click',()=>{previewFraming=!previewFraming;held=previewFraming?'CINEMATIC':null;preview.setAttribute('aria-pressed',String(previewFraming));});
    previous.addEventListener('click',()=>stepAspect(-1));next.addEventListener('click',()=>stepAspect(1));
    refreshAspect=()=>{
      const preset=ASPECT_PRESETS[aspectIndex];
      const geometry=aspectGeometry(w/h,preset,cfg.cinematicBarsPercent);
      const percent=geometry.top;
      const ratio=(w/h)/Math.max(.01,1-2*percent/100);
      label.textContent=preset.label+' · '+(geometry.side>0?'side bars '+geometry.side.toFixed(1)+'%':percent>0?'top / bottom '+percent.toFixed(1)+'%':'full frame');
      sample.style.setProperty('--frame-bar',percent+'%');
      sample.style.setProperty('--frame-side',geometry.side+'%');
    };
    framing.append(previous,sample,label,next,preview);root.append(framing);refreshAspect();

    if ('controls' in root.dataset) root.append(controls(() => cfg, () => zoom));
  } else {
    // A demo: play/pause and restart, and its caption. With reduced motion it waits to be played.
    const pause = button('Pause'), restart = button('Restart');
    self.note = document.createElement('span');
    self.note.className = 'cze-view__note';
    bar.append(pause, self.note, restart);
    stage.setAttribute('role', 'img');
    stage.setAttribute('aria-label', 'An animation of the zoom. The caption under it says what it shows.');
    let paused = matchMedia('(prefers-reduced-motion: reduce)').matches;
    const show = () => {
      pause.textContent = paused ? 'Play' : 'Pause';
      pause.setAttribute('aria-pressed', String(paused));
    };
    show();
    pause.addEventListener('click', () => {
      paused = !paused;
      show();
    });
    const start = () => {
      build();
      demo = new Run(script, { cfg: () => cfg, zoom: () => zoom, hold: m => (held = m), keys: k => showKeys(k) });
    };
    restart.addEventListener('click', () => {
      start();
      paused = false;
      show();
    });
    start();
    self.paused = () => paused;
    self.restart = start;
  }

  // Frames, only while on screen.
  let visible = false, last = performance.now(), restartIn = -1;
  new IntersectionObserver(([e]) => {
    visible = e.isIntersecting;
    if (visible) requestAnimationFrame(frame);
  }).observe(root);
  const ctx = canvas.getContext('2d');

  function frame(now) {
    const ms = Math.min(250, now - last);
    last = now;
    const running = !demo || !self.paused();
    if (demo && running) {
      if (demo.done) {
        if (restartIn < 0) restartIn = 2000;
        if ((restartIn -= ms) <= 0) {
          restartIn = -1;
          self.restart();
        }
      } else {
        demo.tick(ms);
      }
    }
    if (running) {
      // ZoomInputState: the held key's profile, or none; ZoomController then eases toward it.
      if (held !== mode) {
        mode = held || 'NONE';
        zoom.update(mode);
      }
      if (!script && mode === 'CINEMATIC' && aspectIndex >= 0) {
        const g=aspectGeometry(w/h,ASPECT_PRESETS[aspectIndex],cfg.cinematicBarsPercent);
        zoom.heldBarsPercent=g.top;zoom.targetBarsPercent=g.top;targetSidePercent=g.side;
      }
      const desiredSide=mode==='CINEMATIC'?targetSidePercent:0;
      sidePercent+=(desiredSide-sidePercent)*(1-Math.exp(-ms/60));
      zoom.frame(ms);
      if (!script) refreshAspect();
      // Vanilla's smooth camera while the cinematic profile asks for it; the title screen's slow turn when nobody looks.
      const smooth = mode === 'CINEMATIC' ? cfg.cinematicCamera : mode === 'REGULAR' ? cfg.regularCinematicCamera : false;
      if (demo) look.yaw += ms * 0.004;
      const k = smooth ? 1 - Math.exp(-ms / 180) : 1;
      yaw += (look.yaw - yaw) * k;
      pitch += (look.pitch - pitch) * k;
    }
    keysLeft -= ms;
    keys.classList.toggle('is-shown', keysLeft > 0);

    // The world: a pinhole camera with the game's vertical field of view.
    const fov = zoom.fov(), p = (h / 2) / Math.tan(fov * Math.PI / 360);
    world.style.perspective = p + 'px';
    cam.style.transform = `translateZ(${p}px) rotateX(${pitch}deg) rotateY(${yaw}deg)`;

    // Bars first, then the HUD, as the mod draws them; the HUD and crosshair go while a profile that hides them is held.
    ctx.setTransform(scale, 0, 0, scale, 0, 0);
    ctx.imageSmoothingEnabled = false;
    ctx.clearRect(0, 0, canvas.width / scale, canvas.height / scale);
    const hides = what => mode !== 'NONE' && cfg[(mode === 'REGULAR' ? 'regular' : 'cinematic') + what];
    const barsPct = zoom.currentBarsPercent, barH = Math.round(guiH * barsPct / 100);
    if (barsPct > 1e-4 && barH > 0) {
      ctx.fillStyle = '#000';
      // To the canvas's very edges, past the last whole GUI pixel, so none of the world shows beside the bars.
      const fullW = canvas.width / scale, fullH = canvas.height / scale;
      ctx.fillRect(0, 0, fullW, barH);
      ctx.fillRect(0, fullH - barH, fullW, barH);
    }
    if (sidePercent > .01) {
      ctx.fillStyle='#000';const side=canvas.width/scale*sidePercent/100;
      ctx.fillRect(0,0,side,canvas.height/scale);ctx.fillRect(canvas.width/scale-side,0,side,canvas.height/scale);
    }
    if (!hides('HideHud')) drawHud(ctx, sprites, guiW, guiH);
    if (!hides('HideCrosshair')) blit(ctx, sprites, 'hud/crosshair', Math.floor((guiW - 15) / 2), Math.floor((guiH - 15) / 2), 15, 15);
    if (demo && demo.graph) drawGraph(ctx, demo.graph, zoom, guiW, guiH);

    const zoomTimes = 1 / zoom.currentMultiplier;
    const reading = `${zoomTimes >= 10 ? zoomTimes.toFixed(0) : zoomTimes.toFixed(1)}× · ${fov.toFixed(0)}° field of view${barsPct > 0.05 ? ` · bars ${barsPct.toFixed(0)}%` : ''}`;
    self.note.textContent = demo ? demo.caption : reading;
    self.pressed?.();
    if (visible || document.fullscreenElement === root) requestAnimationFrame(frame);
  }
}

function button(label) {
  const b = document.createElement('button');
  b.type = 'button';
  b.textContent = label;
  return b;
}

function blit(ctx, sprites, key, x, y, w, h) {
  const img = sprites.get(key);
  if (img) ctx.drawImage(img, 0, 0, img.width, Math.min(img.height, img.width * h / w), x, y, w, h);
}

const ITEMS = ['diamond_sword', 'iron_pickaxe', 'bow', 'spyglass', 'apple', 'bread', 'map', 'compass_00'];

/** The game's HUD in survival: hotbar with a few things in it, experience, hearts, armor and food, where Gui puts them. */
function drawHud(ctx, sprites, w, h) {
  const x0 = Math.floor(w / 2) - 91;
  blit(ctx, sprites, 'hud/hotbar', x0, h - 22, 182, 22);
  blit(ctx, sprites, 'hud/hotbar_selection', x0 - 1, h - 23, 24, 23);
  ITEMS.forEach((item, i) => blit(ctx, sprites, 'item/' + item, x0 + 3 + i * 20, h - 19, 16, 16));
  blit(ctx, sprites, 'hud/experience_bar_background', x0, h - 29, 182, 5);
  const xp = sprites.get('hud/experience_bar_progress');
  if (xp) ctx.drawImage(xp, 0, 0, Math.round(xp.width * 0.62), xp.height, x0, h - 29, Math.round(182 * 0.62), 5);
  for (let i = 0; i < 10; i++) {
    blit(ctx, sprites, 'hud/heart/container', x0 + i * 8, h - 39, 9, 9);
    blit(ctx, sprites, 'hud/heart/full', x0 + i * 8, h - 39, 9, 9);
    if (i < 7) blit(ctx, sprites, 'hud/armor_full', x0 + i * 8, h - 49, 9, 9);
    const fx = x0 + 182 - i * 8 - 9;
    blit(ctx, sprites, 'hud/food_empty', fx, h - 39, 9, 9);
    if (i < 9) blit(ctx, sprites, 'hud/food_full', fx, h - 39, 9, 9);
  }
}

/** The easing curve in a corner, with a dot where the zoom is on it. */
function drawGraph(ctx, g, zoom, w, h) {
  const size = 64, x = w - size - 10, y = 10 + size;
  ctx.fillStyle = 'rgba(0,0,0,0.55)';
  ctx.fillRect(x - 6, y - size - 6, size + 12, size + 12);
  ctx.strokeStyle = 'rgba(255,255,255,0.25)';
  ctx.lineWidth = 1;
  ctx.strokeRect(x + 0.5, y - size + 0.5, size - 1, size - 1);
  ctx.strokeStyle = '#fff';
  ctx.beginPath();
  for (let i = 0; i <= size; i++) {
    const t = i / size, v = curve(g.curve, t);
    ctx[i ? 'lineTo' : 'moveTo'](x + i, y - v * (size - 1));
  }
  ctx.stroke();
  const t = Math.min(1, zoom.animElapsedMs / Math.max(1, zoom.animDurationMs));
  if (zoom.animCurve === g.curve && zoom.currentMultiplier !== zoom.targetMultiplier) {
    ctx.fillStyle = '#ffd34d';
    ctx.fillRect(x + Math.round(t * size) - 2, y - Math.round(curve(g.curve, t) * (size - 1)) - 2, 4, 4);
  }
}

// --------------------------------------------------------------- settings

/** A drop-down in the page's greys: a button showing the choice and a list under it, with the keys a select has. */
let pickers = 0;
function picker(options, value, set) {
  const wrap = document.createElement('span');
  wrap.className = 'cze-view__picker';
  const id = `cze-picker-${++pickers}`;
  const btn = Object.assign(document.createElement('button'), { type: 'button' });
  btn.setAttribute('aria-haspopup', 'listbox');
  btn.setAttribute('aria-expanded', 'false');
  btn.setAttribute('aria-controls', id);
  const list = Object.assign(document.createElement('ul'), { id, hidden: true, tabIndex: -1 });
  list.setAttribute('role', 'listbox');
  const keys = Object.keys(options);
  const items = keys.map(k => {
    const li = Object.assign(document.createElement('li'), { id: `${id}-${k}`, textContent: options[k] });
    li.setAttribute('role', 'option');
    li.addEventListener('pointerdown', e => e.preventDefault());
    li.addEventListener('click', e => {
      // The list sits in a label, which would pass the click on to the button and open it again.
      e.preventDefault();
      choose(k);
      close();
    });
    li.addEventListener('pointermove', () => mark(keys.indexOf(k)));
    list.append(li);
    return li;
  });
  let active = 0;
  const mark = i => {
    active = (i + keys.length) % keys.length;
    items.forEach((li, j) => li.classList.toggle('is-active', j === active));
    list.setAttribute('aria-activedescendant', items[active].id);
    items[active].scrollIntoView({ block: 'nearest' });
  };
  const choose = k => {
    value = k;
    btn.textContent = options[k];
    items.forEach((li, j) => li.setAttribute('aria-selected', String(keys[j] === k)));
    set(k);
  };
  const open = () => {
    list.hidden = false;
    btn.setAttribute('aria-expanded', 'true');
    mark(keys.indexOf(value));
    list.focus({ preventScroll: true });
  };
  const close = (refocus = true) => {
    if (list.hidden) return;
    list.hidden = true;
    btn.setAttribute('aria-expanded', 'false');
    if (refocus) btn.focus({ preventScroll: true });
  };
  btn.addEventListener('click', e => {
    e.preventDefault();
    list.hidden ? open() : close();
  });
  btn.addEventListener('keydown', e => {
    if (['ArrowDown', 'ArrowUp', 'Enter', ' '].includes(e.key)) {
      e.preventDefault();
      open();
    }
  });
  list.addEventListener('keydown', e => {
    const step = { ArrowDown: 1, ArrowUp: -1 }[e.key];
    if (step) mark(active + step);
    else if (e.key === 'Home') mark(0);
    else if (e.key === 'End') mark(keys.length - 1);
    else if (e.key === 'Enter' || e.key === ' ') {
      choose(keys[active]);
      close();
    } else if (e.key === 'Escape') close();
    else if (e.key === 'Tab') return close(false);
    else if (e.key.length === 1) {
      // Type-ahead: the next option starting with the letter.
      const c = e.key.toLowerCase();
      const i = keys.findIndex((k, j) => j > active && options[k].toLowerCase().startsWith(c));
      const first = keys.findIndex(k => options[k].toLowerCase().startsWith(c));
      if (i >= 0 || first >= 0) mark(i >= 0 ? i : first);
    } else return;
    e.preventDefault();
  });
  list.addEventListener('focusout', e => {
    if (!wrap.contains(e.relatedTarget)) close(false);
  });
  choose(value);
  wrap.append(btn, list);
  return wrap;
}

/** The settings to try beside the view: the cinematic profile's curve, time, starting zoom and bars, and the wheel's. */
function controls(cfg, zoom) {
  const form = document.createElement('form');
  form.className = 'cze-view__controls';
  form.addEventListener('submit', e => e.preventDefault());
  const row = (label, input, out) => {
    const l = document.createElement('label');
    l.append(Object.assign(document.createElement('span'), { textContent: label }));
    if (out) l.append(out);
    l.append(input);
    form.append(l);
  };
  const range = (min, max, step, get, set, show) => {
    const i = Object.assign(document.createElement('input'), { type: 'range', min, max, step, value: get() });
    const o = document.createElement('output');
    const sync = () => (o.textContent = show(Number(i.value)));
    i.addEventListener('input', () => {
      set(Number(i.value));
      sync();
    });
    sync();
    return [i, o];
  };
  row('Easing curve', picker(CURVES, cfg().cinematicZoomInCurve, v => {
    const c = cfg();
    c.cinematicZoomInCurve = c.cinematicZoomOutCurve = c.regularZoomInCurve = c.regularZoomOutCurve = v;
  }));
  row('Zoom duration', ...range(0, 1000, 20, () => cfg().cinematicZoomInMs, v => {
    const c = cfg();
    c.cinematicZoomInMs = c.cinematicZoomOutMs = c.regularZoomInMs = c.regularZoomOutMs = v;
  }, v => `${v} ms`));
  row('Starting zoom', ...range(1, 10, 0.5, () => cfg().cinematicStartingZoom, v => (cfg().cinematicStartingZoom = cfg().regularStartingZoom = v), v => `${v}×`));
  row('Bar height', ...range(0, 50, 1, () => cfg().cinematicBarsPercent, v => (cfg().cinematicBarsPercent = v), v => `${v}%`));
  const snaps = Object.assign(document.createElement('input'), { type: 'text', placeholder: '2x, 4x, 45fov', spellcheck: false });
  snaps.addEventListener('input', () => (cfg().cinematicSnapPoints = cfg().regularSnapPoints = snaps.value));
  row('Target values', snaps);
  const checks = Object.assign(document.createElement('div'), { className: 'cze-view__checks' });
  form.append(checks);
  const check = (label, get, set) => {
    const i = Object.assign(document.createElement('input'), { type: 'checkbox', checked: get() });
    i.addEventListener('change', () => set(i.checked));
    const l = document.createElement('label');
    l.className = 'is-check';
    l.append(i, Object.assign(document.createElement('span'), { textContent: label }));
    checks.append(l);
  };
  check('Hide HUD', () => cfg().cinematicHideHud, v => (cfg().cinematicHideHud = cfg().regularHideHud = v));
  check('Hide crosshair', () => cfg().cinematicHideCrosshair, v => (cfg().cinematicHideCrosshair = cfg().regularHideCrosshair = v));
  check('Toggle zoom', () => cfg().cinematicToggle, v => (cfg().cinematicToggle = cfg().regularToggle = v));
  check('Allow ultrawide zoom (0.5×)', () => cfg().allowUltrawide, v => setUltrawide(cfg(), v));
  check('Magnetize zoom targets', () => cfg().cinematicMagnetize, v => (cfg().cinematicMagnetize = cfg().regularMagnetize = v));
  return form;
}

// ------------------------------------------------------------------- demos

/** A script running a step at a time: each helper yields until its step is done. */
class Run {
  constructor(script, api) {
    Object.assign(this, api);
    this.caption = '';
    this.graph = null;
    this.done = false;
    this.gen = script(this);
    this.gen.next();
  }

  tick(ms) {
    try {
      if (this.gen.next(ms).done) this.done = true;
    } catch (e) {
      console.warn('[cze-view] demo step failed', e);
      this.done = true;
    }
  }

  *wait(ms) {
    for (let t = 0; t < ms; ) t += yield;
  }

  *say(text) {
    this.caption = text;
    yield* this.wait(Math.min(1400, 400 + text.length * 14));
  }

  press(mode, label) {
    this.hold(mode);
    this.keys(label);
  }

  *wheel(v, times, gap = 140) {
    for (let i = 0; i < times; i++) {
      this.zoom().wheel(v);
      this.keys(v > 0 ? 'Scroll ↑' : 'Scroll ↓');
      yield* this.wait(gap);
    }
  }

  *bars(delta, times, gap = 70) {
    for (let i = 0; i < times; i++) {
      this.zoom().adjustBars(delta);
      this.keys(delta > 0 ? '↑' : '↓');
      yield* this.wait(gap);
    }
  }
}

const DEMOS = {
  // The whole thing: hold, scroll, resize the bars, let go.
  *tour(d) {
    yield* d.say('Hold C: the view eases in, the bars slide in, and the HUD and crosshair go.');
    d.press('CINEMATIC', 'C held');
    yield* d.wait(1800);
    yield* d.say('Scroll to zoom further in, or back out.');
    yield* d.wheel(1, 6);
    yield* d.wait(700);
    yield* d.wheel(-1, 4);
    yield* d.wait(500);
    yield* d.say('↑ and ↓ make the bars taller or shorter.');
    yield* d.bars(1, 10);
    yield* d.wait(600);
    yield* d.bars(-1, 10);
    yield* d.wait(400);
    yield* d.say('← and → cycle framing presets in the web preview.');
    for (const ratio of [1.85, 2.35, 2.39]) {
      d.zoom().adjustBars(barsForAspect(16/9,ratio)-d.zoom().heldBarsPercent);
      d.keys('→ '+ratio+':1');
      yield* d.wait(1200);
    }
    yield* d.say('Let go of C and it eases back out.');
    d.press(null, 'C let go');
    yield* d.wait(2200);
  },

  // Each easing curve, slowed down so its shape shows.
  *curves(d) {
    const cfg = d.cfg();
    cfg.cinematicZoomInMs = cfg.cinematicZoomOutMs = 900;
    for (const [k, name] of Object.entries(CURVES)) {
      cfg.cinematicZoomInCurve = cfg.cinematicZoomOutCurve = k;
      d.graph = { curve: k };
      d.caption = `${name}, slowed to 900 ms to show its shape (240 ms by default).`;
      d.press('CINEMATIC', 'C held');
      yield* d.wait(1500);
      d.press(null, 'C let go');
      yield* d.wait(1400);
    }
  },

  // Snap targets with the wheel snapping to them.
  *snaps(d) {
    const cfg = d.cfg();
    cfg.cinematicSnapPoints = '2x, 4x, 30fov, 8x';
    cfg.cinematicMagnetize = true;
    cfg.cinematicStartingZoom = 2;
    yield* d.say('Snap targets 2x, 4x, 30fov and 8x, with the wheel snapping to them.');
    d.press('CINEMATIC', 'C held');
    yield* d.wait(1000);
    for (const v of [1, 1, 1, -1, -1, -1]) {
      d.caption = v > 0 ? 'Each notch in goes to the next target.' : 'Each notch out goes back one.';
      yield* d.wheel(v, 1);
      yield* d.wait(1000);
    }
    d.press(null, 'C let go');
    yield* d.wait(1600);
  },

  // The two profiles: with bars, and without.
  *profiles(d) {
    yield* d.say('Cinematic Zoom (C): bars, the smooth camera, and its own starting zoom.');
    d.press('CINEMATIC', 'C held');
    yield* d.wait(1800);
    d.press(null, 'C let go');
    yield* d.wait(900);
    yield* d.say('Zoom without bars, on its own key: no bars, the normal camera, and its own settings.');
    d.press('REGULAR', 'Key held');
    yield* d.wait(1800);
    d.press(null, 'Key let go');
    yield* d.wait(1600);
  },

  // Ultra-wide: past 1× to 0.5×.
  *ultrawide(d) {
    const cfg = d.cfg();
    setUltrawide(cfg, true);
    cfg.regularStartingZoom = 1;
    yield* d.say('With Ultra-wide zoom on, hold the zoom key and scroll back past 1×.');
    d.press('REGULAR', 'Key held');
    yield* d.wait(500);
    yield* d.wheel(-1, 20, 110);
    yield* d.say('It stops at 0.5×: twice the field of view, up to 175°.');
    yield* d.wait(1600);
    d.press(null, 'Key let go');
    yield* d.wait(1600);
  },
};

for (const root of document.querySelectorAll('.cze-view')) {
  mount(root).catch(e => {
    root.textContent = "The zoom view didn't load.";
    console.error('[cze-view]', e);
  });
}
