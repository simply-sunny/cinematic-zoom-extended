// The mod's zoom, ported from ZoomCurve, ZoomSnapPoints and ZoomController in src/common: the same curves,
// the same snap parsing and the same frame-by-frame animation, so the page zooms as the game does.

export const CURVES = {
  EXPONENTIAL: 'Ease Out (Exp)',
  EASE_OUT: 'Ease Out (Cubic)',
  EASE_IN: 'Ease In (Cubic)',
  EASE_IN_OUT: 'Ease In/Out',
  LINEAR: 'Linear',
  SINE: 'Sine',
};

export function curve(name, t) {
  if (t <= 0) return 0;
  if (t >= 1) return 1;
  switch (name) {
    case 'LINEAR': return t;
    case 'EASE_IN': return t * t * t;
    case 'EASE_OUT': return 1 - (1 - t) ** 3;
    case 'EASE_IN_OUT': return t < 0.5 ? 4 * t * t * t : 1 - (-2 * t + 2) ** 3 / 2;
    case 'SINE': return Math.sin(t * (Math.PI / 2));
    default: {
      const k = 4.605170185988092; // ln(100): reaches ~99% at t=1
      return (1 - Math.exp(-k * t)) / (1 - Math.exp(-k));
    }
  }
}

/** ZoomConfig's defaults. */
export function defaultConfig() {
  return {
    cinematicStartingZoom: 3, cinematicBarsPercent: 15, arrowKeysAdjustBars: true, barAdjustStep: 1, smoothBarChange: true,
    preserveBarAdjustment: false, cinematicZoomInMs: 240, cinematicZoomOutMs: 240, cinematicZoomInCurve: 'EXPONENTIAL',
    cinematicZoomOutCurve: 'EXPONENTIAL', cinematicHideHud: true, cinematicHideCrosshair: true, cinematicHideHands: true,
    cinematicCamera: true, cinematicScaleSensitivity: true, cinematicRemoveBobbing: true, cinematicToggle: false,
    cinematicMagnetize: false, cinematicSnapPoints: '',
    regularStartingZoom: 3, regularZoomInMs: 240, regularZoomOutMs: 240, regularZoomInCurve: 'EXPONENTIAL',
    regularZoomOutCurve: 'EXPONENTIAL', regularHideHud: true, regularHideCrosshair: true, regularHideHands: true,
    regularCinematicCamera: false, regularScaleSensitivity: true, regularRemoveBobbing: true, regularToggle: false,
    regularMagnetize: false, regularSnapPoints: '',
    mouseWheelEnabled: true, discreteScroll: false, discreteScrollStep: 1, allowUltrawide: false,
    minZoomMultiplier: 1 / 30, maxZoomMultiplier: 1, wheelStep: 0.05,
  };
}

/** Ultra-wide zoom lets the multiplier past 1, to 2 (0.5×), as ZoomConfig sets it. */
export function setUltrawide(cfg, on) {
  cfg.allowUltrawide = on;
  cfg.maxZoomMultiplier = on ? 2 : 1;
}

const clamp = (v, lo, hi) => Math.max(lo, Math.min(hi, v));
const lerp = (a, b, t) => a + (b - a) * t;

/** Snap targets as multipliers: "2x" is a zoom, "45fov" (or "45deg", "45°", "45f", or a bare 20 and up) a field of view. */
export function parseSnaps(raw, baseFov, minMul, maxMul) {
  if (!raw || !raw.trim()) return [];
  const list = [];
  for (const part of raw.split(',')) {
    const token = part.trim().toLowerCase();
    if (!token) continue;
    let val, isFov;
    if (token.endsWith('fov')) [val, isFov] = [parseFloat(token.slice(0, -3)), true];
    else if (token.endsWith('deg')) [val, isFov] = [parseFloat(token.slice(0, -3)), true];
    else if (token.endsWith('°')) [val, isFov] = [parseFloat(token.slice(0, -1)), true];
    else if (token.endsWith('x')) [val, isFov] = [parseFloat(token.slice(0, -1)), false];
    else if (token.endsWith('f')) [val, isFov] = [parseFloat(token.slice(0, -1)), true];
    else [val, isFov] = [parseFloat(token), parseFloat(token) >= 20];
    if (!(val > 0)) continue;
    let mul = Math.fround(isFov ? val / baseFov : 1 / val);
    if (mul >= minMul - 1e-4 && mul <= maxMul + 1e-4) {
      mul = clamp(mul, minMul, maxMul);
      if (!list.includes(mul)) list.push(mul);
    }
  }
  return list.sort((a, b) => a - b);
}

export function stepBetweenSnaps(current, snaps, vertical) {
  if (!vertical || !snaps.length) return current;
  const notches = Math.max(1, Math.round(Math.abs(vertical)));
  let result = current;
  for (let i = 0; i < notches; i++) {
    if (vertical > 0) {
      const smaller = snaps.filter(s => s < result - 1e-4);
      if (!smaller.length) return snaps[0];
      result = Math.max(...smaller);
    } else {
      const larger = snaps.filter(s => s > result + 1e-4);
      if (!larger.length) return snaps[snaps.length - 1];
      result = Math.min(...larger);
    }
  }
  return result;
}

/** ZoomController: the multiplier on the field of view and the bars' height, eased toward where the keys and wheel want them. */
export class Zoom {
  constructor(cfg) {
    this.cfg = cfg;
    this.reset();
  }

  reset() {
    this.activeMode = 'NONE';
    this.lastActiveMode = 'CINEMATIC';
    this.currentMultiplier = this.targetMultiplier = 1;
    this.heldMultiplier = this.startingMultiplier('CINEMATIC');
    this.heldBarsPercent = this.cfg.cinematicBarsPercent;
    this.currentBarsPercent = this.targetBarsPercent = 0;
    this.animStartMultiplier = 1;
    this.animStartBarsPercent = 0;
    this.animElapsedMs = 0;
    this.animDurationMs = this.cfg.cinematicZoomOutMs;
    this.animCurve = this.cfg.cinematicZoomOutCurve;
    this.baseFov = 70;
  }

  /** {@code mode}: NONE, CINEMATIC or REGULAR, as the keys ask this frame. */
  update(mode) {
    const starting = mode !== 'NONE' && this.activeMode === 'NONE';
    if (mode !== 'NONE') this.lastActiveMode = mode;
    if (starting) {
      this.heldMultiplier = this.startingMultiplier(mode);
      this.heldBarsPercent = this.cfg.cinematicBarsPercent;
    }
    this.activeMode = mode;
    const prevMul = this.targetMultiplier, prevBars = this.targetBarsPercent;
    if (mode === 'CINEMATIC') {
      this.targetMultiplier = this.heldMultiplier;
      this.targetBarsPercent = this.heldBarsPercent;
    } else if (mode === 'REGULAR') {
      this.targetMultiplier = this.heldMultiplier;
      this.targetBarsPercent = 0;
    } else {
      this.targetMultiplier = 1;
      this.targetBarsPercent = 0;
    }
    if (this.targetMultiplier !== prevMul || this.targetBarsPercent !== prevBars) this.startAnimation(mode !== 'NONE');
  }

  /** A frame of {@code deltaMs} milliseconds (at most 50, as the mod caps it). */
  frame(deltaMs) {
    deltaMs = Math.min(deltaMs, 50);
    if (this.currentMultiplier === this.targetMultiplier && this.currentBarsPercent === this.targetBarsPercent) return;
    if (this.currentMultiplier === this.targetMultiplier) {
      if (this.cfg.smoothBarChange) {
        const factor = 1 - Math.exp(-deltaMs / 60);
        this.currentBarsPercent += (this.targetBarsPercent - this.currentBarsPercent) * factor;
        if (Math.abs(this.currentBarsPercent - this.targetBarsPercent) < 1e-3) this.currentBarsPercent = this.targetBarsPercent;
      } else {
        this.currentBarsPercent = this.targetBarsPercent;
      }
      return;
    }
    if (this.animDurationMs <= 0) {
      this.currentMultiplier = this.targetMultiplier;
      this.currentBarsPercent = this.targetBarsPercent;
      return;
    }
    this.animElapsedMs += deltaMs;
    const progress = Math.min(1, this.animElapsedMs / this.animDurationMs), eased = curve(this.animCurve, progress);
    this.currentMultiplier = lerp(this.animStartMultiplier, this.targetMultiplier, eased);
    this.currentBarsPercent = lerp(this.animStartBarsPercent, this.targetBarsPercent, eased);
    if (progress >= 1 || Math.abs(this.currentMultiplier - this.targetMultiplier) < 1e-4) {
      this.currentMultiplier = this.targetMultiplier;
      this.currentBarsPercent = this.targetBarsPercent;
    }
  }

  startAnimation(zoomingIn) {
    const cfg = this.cfg, regular = this.lastActiveMode === 'REGULAR';
    this.animStartMultiplier = this.currentMultiplier;
    this.animStartBarsPercent = this.currentBarsPercent;
    this.animElapsedMs = 0;
    this.animDurationMs = regular ? (zoomingIn ? cfg.regularZoomInMs : cfg.regularZoomOutMs) : (zoomingIn ? cfg.cinematicZoomInMs : cfg.cinematicZoomOutMs);
    this.animCurve = (regular ? (zoomingIn ? cfg.regularZoomInCurve : cfg.regularZoomOutCurve)
      : (zoomingIn ? cfg.cinematicZoomInCurve : cfg.cinematicZoomOutCurve)) || 'EXPONENTIAL';
  }

  /** The wheel while zoomed: up ({@code vertical} > 0) zooms in. False if the wheel isn't the zoom's right now. */
  wheel(vertical) {
    const cfg = this.cfg;
    if (this.activeMode === 'NONE' || !cfg.mouseWheelEnabled || !vertical) return false;
    const prev = this.heldMultiplier, mode = this.activeMode, regular = mode === 'REGULAR';
    const snaps = parseSnaps(regular ? cfg.regularSnapPoints : cfg.cinematicSnapPoints, this.baseFov, cfg.minZoomMultiplier, cfg.maxZoomMultiplier);
    if ((regular ? cfg.regularMagnetize : cfg.cinematicMagnetize) && snaps.length) {
      this.heldMultiplier = stepBetweenSnaps(this.heldMultiplier, snaps, vertical);
    } else if (cfg.discreteScroll) {
      const zoom = this.heldMultiplier <= 0 ? 1 : 1 / this.heldMultiplier, step = Math.max(0.1, cfg.discreteScrollStep);
      const notches = Math.max(1, Math.round(Math.abs(vertical)));
      let next = vertical > 0 ? (Math.floor(zoom / step + 1e-4) + notches) * step : (Math.ceil(zoom / step - 1e-4) - notches) * step;
      next = clamp(next, 1 / cfg.maxZoomMultiplier, 1 / cfg.minZoomMultiplier);
      this.heldMultiplier = 1 / next;
    } else {
      this.heldMultiplier = clamp(this.heldMultiplier - vertical * cfg.wheelStep, cfg.minZoomMultiplier, cfg.maxZoomMultiplier);
    }
    this.targetMultiplier = this.heldMultiplier;
    if (this.targetMultiplier !== prev) this.startAnimation(true);
    return true;
  }

  /** The arrow keys in the cinematic profile: the bars a step taller or shorter, 0 to 50%. */
  adjustBars(delta) {
    if (this.activeMode !== 'CINEMATIC') return false;
    this.heldBarsPercent = clamp(this.heldBarsPercent + delta, 0, 50);
    if (this.cfg.preserveBarAdjustment) this.cfg.cinematicBarsPercent = this.heldBarsPercent;
    this.targetBarsPercent = this.heldBarsPercent;
    if (!this.cfg.smoothBarChange) this.currentBarsPercent = this.heldBarsPercent;
    return true;
  }

  isActive() {
    return this.activeMode !== 'NONE' || Math.abs(this.currentMultiplier - 1) > 1e-4 || this.currentBarsPercent > 1e-3;
  }

  sensitivity() {
    if (!this.isActive()) return 1;
    const scale = this.lastActiveMode === 'REGULAR' ? this.cfg.regularScaleSensitivity : this.cfg.cinematicScaleSensitivity;
    return scale ? Math.min(1, this.currentMultiplier) : 1;
  }

  /** The field of view on screen, as Camera.calculateFov returns it with the zoom applied. */
  fov() {
    return Math.min(175, this.baseFov * this.currentMultiplier);
  }

  startingMultiplier(mode) {
    const zoom = mode === 'REGULAR' ? this.cfg.regularStartingZoom : this.cfg.cinematicStartingZoom;
    return clamp(zoom <= 0 ? 1 : 1 / zoom, this.cfg.minZoomMultiplier, this.cfg.maxZoomMultiplier);
  }
}
