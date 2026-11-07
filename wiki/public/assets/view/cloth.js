// The mod's settings screen as the game draws it: Cloth Config over the blurred world. Numbers are measured from the
// running game at 480×270 and 960×540; the rows come from settings.json (tools/settings-data.py), read from the mod's source.

const FALLBACK = '8px sans-serif';
const css = c => `#${(c & 0xFFFFFF).toString(16).padStart(6, '0')}`;
const LIST_TOP = 70, ROW = 24, NOTCH = 16, FOOTER = 32;
const GRAY = 0xAAAAAA, WHITE = 0xFFFFFF, DIM = 0xA0A0A0, FIELD = 0xE0E0E0, HINT = 0x707070, GREEN = 0x55FF55, RED = 0xFF5555;

const CURSOR = [
  'O', 'OO', 'OXO', 'OXXO', 'OXXXO', 'OXXXXO', 'OXXXXXO', 'OXXXXXXO', 'OXXXXXXXO', 'OXXXXXOOOO', 'OXXOXXO', 'OXO.OXXO',
  'OO..OXXO', '.....OXXO', '.....OXXO', '......OO',
];

/** The game's font, from the sheets in its jar. */
export class Font {
  constructor() {
    this.ctx = document.createElement('canvas').getContext('2d');
    this.ctx.font = FALLBACK;
    this.glyphs = new Map([[' ', { advance: 4 }]]);
    this.tints = new Map();
  }

  load({ providers, sheets }) {
    for (const { file, chars, ascent, height = 8 } of providers) {
      const sheet = sheets.get(file), cols = [...chars[0]].length, cw = sheet.width / cols, ch = sheet.height / chars.length, scale = height / ch;
      const canvas = Object.assign(document.createElement('canvas'), { width: sheet.width, height: sheet.height });
      const ctx = canvas.getContext('2d', { willReadFrequently: true });
      ctx.drawImage(sheet, 0, 0);
      const { data } = ctx.getImageData(0, 0, sheet.width, sheet.height);
      const inked = (x, y) => { for (let i = 0; i < ch; i++) if (data[((y + i) * sheet.width + x) * 4 + 3]) return true; return false; };
      chars.forEach((row, j) => [...row].forEach((c, i) => {
        if (c === '\0' || this.glyphs.has(c)) return;
        const sx = i * cw, sy = j * ch;
        let w = cw;
        while (w > 0 && !inked(sx + w - 1, sy)) w--;
        this.glyphs.set(c, { sheet, sx, sy, sw: w, sh: ch, dw: w * scale, dh: height, top: 7 - ascent, advance: Math.floor(0.5 + w * scale) + 1 });
      }));
    }
  }

  glyph(c) {
    let g = this.glyphs.get(c);
    if (!g) this.glyphs.set(c, g = { text: true, advance: Math.ceil(this.ctx.measureText(c).width) });
    return g;
  }

  tint(sheet, rgb) {
    let tints = this.tints.get(rgb);
    if (!tints) this.tints.set(rgb, tints = new Map());
    let canvas = tints.get(sheet);
    if (!canvas) {
      canvas = Object.assign(document.createElement('canvas'), { width: sheet.width, height: sheet.height });
      const ctx = canvas.getContext('2d');
      ctx.drawImage(sheet, 0, 0);
      ctx.globalCompositeOperation = 'source-in';
      ctx.fillStyle = css(rgb);
      ctx.fillRect(0, 0, sheet.width, sheet.height);
      tints.set(sheet, canvas);
    }
    return canvas;
  }

  draw(ctx, s, x, y, rgb) {
    for (const char of s) {
      const g = this.glyph(char);
      if (g.sheet) ctx.drawImage(this.tint(g.sheet, rgb), g.sx, g.sy, g.sw, g.sh, x, y + g.top, g.dw, g.dh);
      else if (g.text) {
        ctx.font = FALLBACK;
        ctx.fillStyle = css(rgb);
        ctx.fillText(char, x, y + 7);
      }
      x += g.advance;
    }
  }

  width(s) {
    let w = 0;
    for (const c of s) w += this.glyph(c).advance;
    return w;
  }
}

export class Cloth {
  constructor(data, font, sprites) {
    Object.assign(this, { data, font, sprites, tab: 0, y: 0, target: 0, edits: new Map(), mx: -20, my: -20, hovered: -1, hoverMs: 0, w: 480, h: 270 });
  }

  get rows() { return this.data.tabs[this.tab].rows; }
  get listBottom() { return this.h - FOOTER; }
  get height() { return 42 + this.rows.length * ROW; }
  get maxScroll() { return Math.max(0, this.height - (this.listBottom - LIST_TOP)); }
  center(i) { return LIST_TOP + 48 + i * ROW - this.y; }
  value(i) { const k = `${this.tab}:${i}`; return this.edits.has(k) ? this.edits.get(k) : this.rows[i].value; }
  edited(i) { return this.edits.has(`${this.tab}:${i}`); }

  tabs() {
    let x = 24;
    return this.data.tabs.map(t => {
      const w = this.font.width(t.name) + 8, r = { x, w };
      x += w + 2;
      return r;
    });
  }

  /** The row called {@code label} on this tab. */
  row(label) { return this.rows.findIndex(r => r.label.startsWith(label)); }

  /** Scrolls until the row is whole in the list. */
  reveal(i) {
    const top = 48 + i * ROW - 12, bottom = top + ROW, view = this.listBottom - LIST_TOP;
    if (top < this.target) this.target = top;
    else if (bottom > this.target + view) this.target = bottom - view;
    this.target = Math.min(this.maxScroll, Math.max(0, this.target));
  }

  wheel(notches) { this.target = Math.min(this.maxScroll, Math.max(0, this.target + notches * NOTCH)); }

  tick(ms) {
    this.y += (this.target - this.y) * (1 - Math.exp(-ms / 60));
    if (Math.abs(this.target - this.y) < .25) this.y = this.target;
    const over = this.rowAt(this.mx, this.my);
    this.hoverMs = over === this.hovered ? this.hoverMs + ms : 0;
    this.hovered = over;
  }

  rowAt(x, y) {
    if (y < LIST_TOP || y >= this.listBottom || x >= this.w - 36) return -1;
    return this.rows.findIndex((_, i) => Math.abs(y - this.center(i)) < ROW / 2);
  }

  click(x, y) {
    const tab = this.tabs().findIndex(t => y >= 43 && y < 63 && x >= t.x && x < t.x + t.w);
    if (tab >= 0) {
      this.tab = tab;
      this.y = this.target = 0;
      return;
    }
    const i = this.rowAt(x, y), row = this.rows[i];
    if (!row || x < this.w - 188 || x >= this.w - 74) return;
    const k = `${this.tab}:${i}`, was = this.value(i);
    const next = row.kind === 'toggle' ? !was : row.kind === 'enum' ? row.options[(row.options.indexOf(was) + 1) % row.options.length] : was;
    if (next === row.value) this.edits.delete(k);
    else this.edits.set(k, next);
  }

  /** A toggle or selector's middle, where a click lands. */
  button(i) { return { x: this.w - 131, y: this.center(i) }; }
  tabCentre(i) { const t = this.tabs()[i]; return { x: t.x + t.w / 2, y: 53 }; }
  saveButton() { const b = this.footer(); return { x: b.save + b.w / 2, y: this.h - 16 }; }

  footer() {
    const w = Math.floor(Math.min(200, (this.w - 62) / 3));
    return { w, cancel: Math.floor(this.w / 2) - w - 3, save: Math.floor(this.w / 2) + 3 };
  }

  text(ctx, s, x, y, rgb) {
    this.font.draw(ctx, s, x + 1, y + 1, (rgb & 0xFCFCFC) >> 2);
    this.font.draw(ctx, s, x, y, rgb);
  }

  /** A vanilla button: its sprite's two ends, as the game's nine-slice looks at this height. */
  widget(ctx, x, y, w, text, rgb, off) {
    const hover = !off && this.mx >= x && this.mx < x + w && this.my >= y && this.my < y + 20;
    const img = this.sprites.get(off ? 'widget/button_disabled' : hover ? 'widget/button_highlighted' : 'widget/button');
    if (img) {
      const half = Math.floor(w / 2);
      ctx.drawImage(img, 0, 0, half, 20, x, y, half, 20);
      ctx.drawImage(img, 200 - (w - half), 0, w - half, 20, x + half, y, w - half, 20);
    }
    this.text(ctx, text, x + Math.floor((w - this.font.width(text)) / 2), y + 6, off ? DIM : rgb);
  }

  field(ctx, x, y, w, h, text, rgb) {
    ctx.fillStyle = '#a0a0a0';
    ctx.fillRect(x, y, w, h);
    ctx.fillStyle = '#000';
    ctx.fillRect(x + 1, y + 1, w - 2, h - 2);
    this.text(ctx, text, x + 4, y + Math.floor((h - 8) / 2), rgb);
  }

  arrow(ctx, x, right) {
    for (let r = 0; r < 18; r++) {
      const n = 9 - Math.abs(r - 8.5) + .5;
      ctx.fillStyle = '#000';
      ctx.fillRect(right ? x : x + 12 - n, 44 + r, n, 1);
      if (n > 3) {
        ctx.fillStyle = '#3a3a3a';
        ctx.fillRect(right ? x + 1 : x + 12 - n + 2, 44 + r, n - 3, 1);
      }
    }
  }

  drawRow(ctx, i) {
    const row = this.rows[i], c = this.center(i), { w } = this;
    if (c < LIST_TOP - 12 || c > this.listBottom + 12) return;
    if (this.hovered === i) {
      ctx.fillStyle = 'rgba(255,255,255,.08)';
      ctx.fillRect(0, c - 10, w, 20);
    }
    // Cloth writes an edited setting's name in white italics, the rest in gray.
    if (this.edited(i)) {
      ctx.save();
      ctx.transform(1, 0, -.25, 1, (c + 3) * .25, 0);
      this.text(ctx, row.label, 42, c - 4, WHITE);
      ctx.restore();
    } else {
      this.text(ctx, row.label, 42, c - 4, GRAY);
    }
    const value = this.value(i);
    if (row.kind === 'toggle') this.widget(ctx, w - 188, c - 10, 114, value ? 'Yes' : 'No', value ? GREEN : RED);
    else if (row.kind === 'enum') this.widget(ctx, w - 188, c - 10, 114, value, WHITE);
    else if (row.kind === 'field') this.field(ctx, w - 186, c - 9, 110, 18, value, FIELD);
    this.widget(ctx, w - 72, c - 10, 34, 'Reset', WHITE, true);
  }

  draw(ctx, w, h) {
    Object.assign(this, { w, h });
    const { font } = this, bottom = this.listBottom;
    ctx.fillStyle = 'rgba(0,0,0,.4)';
    ctx.fillRect(0, LIST_TOP, w, bottom - LIST_TOP);
    ctx.fillStyle = 'rgba(0,0,0,.25)';
    ctx.fillRect(20, 41, w - 40, 24);
    ctx.fillStyle = 'rgba(255,255,255,.2)';
    ctx.fillRect(0, 68, w, 1);
    ctx.fillRect(0, bottom + 1, w, 1);
    ctx.fillStyle = 'rgba(0,0,0,.8)';
    ctx.fillRect(0, 69, w, 1);
    ctx.fillRect(0, bottom, w, 1);

    this.text(ctx, this.data.title, Math.floor((w - font.width(this.data.title)) / 2), 18, WHITE);
    this.tabs().forEach((t, i) => this.widget(ctx, t.x, 43, t.w, this.data.tabs[i].name, WHITE, i === this.tab));
    this.arrow(ctx, 4, false);
    this.arrow(ctx, w - 16, true);

    ctx.save();
    ctx.beginPath();
    ctx.rect(0, LIST_TOP, w, bottom - LIST_TOP);
    ctx.clip();
    const sw = Math.min(500, w - 90);
    this.field(ctx, Math.floor((w - sw) / 2) + 2, LIST_TOP + 10 - this.y, sw, 18, 'Search...', HINT);
    this.rows.forEach((_, i) => this.drawRow(ctx, i));
    const view = bottom - LIST_TOP, handle = Math.max(16, view * view / this.height), top = LIST_TOP + (view - handle) * (this.y / (this.maxScroll || 1));
    ctx.fillStyle = '#000';
    ctx.fillRect(w - 36, LIST_TOP, 6, view);
    ctx.fillStyle = '#808080';
    ctx.fillRect(w - 36, top, 6, handle);
    ctx.fillStyle = '#acacac';
    ctx.fillRect(w - 36, top, 5, handle - 1);
    ctx.restore();

    const b = this.footer(), changed = this.edits.size > 0;
    this.widget(ctx, b.cancel, h - 26, b.w, 'Cancel', WHITE);
    this.widget(ctx, b.save, h - 26, b.w, 'Save & Quit', WHITE, !changed);

    const tip = this.hovered >= 0 && this.hoverMs > 500 && this.rows[this.hovered].tip;
    if (tip) this.tooltip(ctx, tip);
    this.cursor(ctx);
  }

  /** Where the game puts a tooltip: right of the pointer and above it, or left of it where that doesn't fit. */
  tooltip(ctx, tip) {
    const tw = this.font.width(tip), y = this.my - 12;
    let x = this.mx + 12;
    if (x + tw + 4 > this.w) x = Math.max(this.mx - 12 - tw, 4);
    ctx.fillStyle = 'rgba(16,0,16,.94)';
    ctx.fillRect(x - 3, y - 4, tw + 6, 16);
    ctx.fillRect(x - 4, y - 3, tw + 8, 14);
    ctx.fillStyle = 'rgba(80,0,255,.31)';
    ctx.strokeStyle = 'rgba(70,0,200,.5)';
    ctx.lineWidth = 1;
    ctx.strokeRect(x - 2.5, y - 2.5, tw + 5, 13);
    this.text(ctx, tip, x, y, WHITE);
  }

  cursor(ctx) {
    CURSOR.forEach((line, r) => [...line].forEach((c, i) => {
      if (c === '.') return;
      ctx.fillStyle = c === 'O' ? '#000' : '#fff';
      ctx.fillRect(Math.round(this.mx) + i, Math.round(this.my) + r, 1, 1);
    }));
  }
}
