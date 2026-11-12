"""Cozy Cookery art kit v3 - 'clean & cartoony', matched to Wannes's own onion/garlic sprites.

- Soft, smooth gradients (no noise): many close colours from shadow to highlight, light from the top-left.
- Light, warm, slightly pastel fills.
- A 1px warm dark-brown/plum outline (never black), one outline colour per object.
- Compact sprites (~11-13 px) sitting in the middle of the slot.
"""
import math
from PIL import Image


def hx(s):
    s = s.lstrip('#')
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4))


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


import colorsys

SAT = 1.45      # colour punch
CONTRAST = 1.25 # spread between shadow and light
BANDS = 4       # cartoon shading: this many flat tones per material


def punch(c, k):
    h, s, v = colorsys.rgb_to_hsv(*(x / 255 for x in c))
    s = min(1.0, s * SAT)
    v = min(1.0, max(0.0, 0.5 + (v - 0.5) * CONTRAST + (k - 0.5) * 0.08))
    return tuple(int(round(x * 255)) for x in colorsys.hsv_to_rgb(h, s, v))


class Mat:
    """Gradient material: stops dark -> light, plus outline colour."""

    def __init__(self, stops, outline):
        raw = [hx(c) for c in stops]
        self.stops = [punch(c, i / max(1, len(raw) - 1)) for i, c in enumerate(raw)]
        o = colorsys.rgb_to_hsv(*(x / 255 for x in hx(outline)))
        self.outline = tuple(int(round(x * 255)) for x in colorsys.hsv_to_rgb(o[0], min(1, o[1] * 1.2), o[2] * 0.8))

    def at(self, t):
        t = max(0.0, min(1.0, t))
        if BANDS:
            t = min(1.0, round(t * (BANDS - 1)) / (BANDS - 1))
        n = len(self.stops) - 1
        i = min(n - 1, int(t * n))
        return mix(self.stops[i], self.stops[i + 1], t * n - i)


M = {
    # produce (onion/garlic stops sampled from Wannes's own sprites)
    'onion':   Mat(['#a17553', '#bb8d6c', '#cfaa8b', '#dab799', '#e1c7ae', '#e3cfb7'], '#612f25'),
    'onionskin': Mat(['#653626', '#956341', '#b27241', '#c89f81', '#d9b18a'], '#622d28'),
    'garlic':  Mat(['#b58980', '#ca9d8e', '#d3a99f', '#e0beb2', '#e7cdc4', '#edd6cd'], '#5a2e34'),
    'garlicpink': Mat(['#be6453', '#c46a52', '#ce785e', '#d3857f', '#e4947f'], '#5a2e34'),
    'tomato':  Mat(['#a8282a', '#c63a30', '#dc4e36', '#e8643e', '#f07c4a'], '#5e1e22'),
    'leaf':    Mat(['#3e6e2c', '#4f8a36', '#66a444', '#86bc5c', '#a8d27c'], '#25401e'),
    'cabbage': Mat(['#5a8e44', '#6ea252', '#84b462', '#9cc478', '#b4d492'], '#2c4a24'),
    'cabbage_dark': Mat(['#3c6a32', '#4a7c3c', '#5a8e48', '#6aa054'], '#24401e'),
    'iron':    Mat(['#7a7e8c', '#9a9eaa', '#b8bcc6', '#d4d8de', '#eef0f2'], '#3e3c48'),
    'handle':  Mat(['#7a4a2c', '#935c36', '#a87044', '#bc8654'], '#3e2218'),
    'bowl':    Mat(['#7e5030', '#94623a', '#a87446', '#bc8854', '#cc9c66'], '#4a2a1a'),
    'stew':    Mat(['#7a3a1e', '#8e4826', '#a45a30', '#b86e3e'], '#4a2016'),
    'carrot':  Mat(['#d06a24', '#e4842e', '#f0a046', '#f8be6a'], '#6a2e14'),
    'beef':    Mat(['#7a3a24', '#8e4a2e', '#a65e3a', '#bc744a'], '#4a2218'),
    'potato':  Mat(['#d8b670', '#e4c888', '#eed8a2', '#f6e8c2'], '#6a4a26'),
    'plate':   Mat(['#cfc6bc', '#e0d8ce', '#ece6de', '#f6f2ec', '#fdfbf8'], '#6a5a54'),
    'rim':     Mat(['#c8644a', '#d87a5a', '#e4906c'], '#6a5a54'),
    'pasta':   Mat(['#e0b450', '#ecc464', '#f4d47e', '#f9e4a2', '#fdf0c8'], '#7a5424'),
    'sauce':   Mat(['#a8302a', '#c4402e', '#d8583a', '#e8764e', '#f49a72'], '#5e1e1a'),
    'cooked':  Mat(['#9a5426', '#b06830', '#c47e3e', '#d69652', '#e6b070'], '#4e2614'),
    'bone':    Mat(['#d8ccba', '#e6dccc', '#f2ece0', '#fbf8f2'], '#6a5a4c'),
}



class Flat(Mat):
    """Exact cartoon palette: tones dark -> light used as hard bands (no blending), like hand-pixelled art."""

    def __init__(self, tones, outline, cut=(0.42, 0.72, 0.9)):
        self.stops = [hx(c) for c in tones]
        self.outline = hx(outline)
        self.cut = cut

    def at(self, t):
        n = len(self.stops)
        cuts = list(self.cut[:n - 1])
        i = sum(1 for c in cuts if t > c)
        return self.stops[min(i, n - 1)]


# vivid, hue-shifted palettes: shadows lean crimson/plum, lights lean orange/coral (like Wannes's tomato)
M.update({
    'tomato':  Flat(['#941533', '#c03416', '#cf4d3a', '#ff8073'], '#631739'),
    'leaf':    Flat(['#005000', '#047204', '#2b9226', '#4cb03a'], '#003a00'),
    'cabbage': Flat(['#2e7a1e', '#4ea02e', '#76c03e', '#a8e060'], '#14400e'),
    'cabbage_dark': Flat(['#1e5a16', '#2e7a1e', '#3e8e24'], '#10340c'),
    'onion':   Flat(['#a0481a', '#cc6e24', '#e8963a', '#ffc46a'], '#5a1e10'),
    'onionskin': Flat(['#6a2e10', '#a0521c', '#cc7a2a'], '#4a1a0c'),
    'garlic':  Flat(['#c8949e', '#e8c4c8', '#f6e2e0', '#ffffff'], '#6a2a44'),
    'garlicpink': Flat(['#a8467a', '#c8649a'], '#6a2a44'),
    'iron':    Flat(['#5e6276', '#9298ac', '#c8cedc', '#ffffff'], '#26263a'),
    'handle':  Flat(['#6e300e', '#9a4a16', '#c46a24', '#e08c3a'], '#3a160a'),
    'bowl':    Flat(['#6a3414', '#94501e', '#b86e2a', '#d8903c'], '#3a1a0a'),
    'stew':    Flat(['#6a1e0c', '#962e12', '#b44418'], '#3a120a'),
    'carrot':  Flat(['#d05a0a', '#f07a14', '#ffa83a'], '#6a2006'),
    'beef':    Flat(['#5e1e10', '#86321a', '#a84a26'], '#3a100a'),
    'potato':  Flat(['#e0b04a', '#f4d06a', '#fff0a8'], '#7a4a12'),
    'plate':   Flat(['#a8b0c8', '#dce2f0', '#f4f6fc', '#ffffff'], '#3e425e'),
    'rim':     Flat(['#d84a2a', '#f06a3a'], '#3e425e'),
    'pasta':   Flat(['#d8960e', '#f4c22a', '#ffe060', '#fff4a8'], '#7a420a'),
    'sauce':   Flat(['#941533', '#c03416', '#e0502e', '#ff8073'], '#5a1030'),
    'cooked':  Flat(['#a0400c', '#d06a18', '#f0922e', '#ffc06a'], '#4a1808'),
    'bone':    Flat(['#c8b8a4', '#f0e8dc', '#ffffff'], '#5a4a44'),
})


class Pic:
    def __init__(self):
        self.px = {}        # (x,y) -> (rgb, outline_rgb, piece)
        self.piece = 0

    def _put(self, x, y, rgb, mat):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[(x, y)] = (rgb, mat.outline, self.piece)

    def ball(self, cx, cy, rx, ry, mat, lo=0.0, hi=1.0, stripes=0, stripe_amp=0.08, clip=None, shine=True,
             spot=(-0.35, -0.4), ang=0.0):
        """Smoothly lit ellipse. stripes = number of soft vertical layer lines (onion skin, garlic cloves)."""
        m = M[mat]
        self.piece += 1
        best = None
        for y in range(16):
            for x in range(16):
                ux, uy = x + 0.5 - cx, y + 0.5 - cy
                ca, sa = math.cos(ang), math.sin(ang)
                dx, dy = (ux * ca + uy * sa) / rx, (-ux * sa + uy * ca) / ry
                d = dx * dx + dy * dy
                if d > 1 or (clip and not clip(x, y)):
                    continue
                nz = math.sqrt(1 - d)
                l = dx * -0.5 + dy * -0.55 + nz * 0.67          # -1..1
                t = (l + 0.35) / 1.25
                if stripes:
                    t -= stripe_amp * (0.5 + 0.5 * math.cos(dx * math.pi * stripes))
                t = lo + (hi - lo) * t
                self._put(x, y, m.at(t), m)
                ds = (dx - spot[0]) ** 2 + (dy - spot[1]) ** 2
                if best is None or ds < best[0]:
                    best = (ds, x, y)
        if shine and best:
            self._put(best[1], best[2], mix(m.at(1.0), (255, 255, 255), 0.6), m)
            if getattr(self, 'big_shine', True) and (best[1] + 1, best[2]) in self.px:
                self._put(best[1] + 1, best[2], m.at(1.0), m)
        return self


    def toon(self, cx, cy, rx, ry, mat, ang=0.0, shadow=0.30, glint=True, clip=None):
        """Cartoon ball: flat base, clean shadow crescent bottom-right, white-ish glint arc top-left."""
        m = M[mat]
        dark, base, light = m.stops[0], m.stops[1], m.stops[-1]
        self.piece += 1
        ca, sa = math.cos(ang), math.sin(ang)
        for y in range(16):
            for x in range(16):
                ux, uy = x + 0.5 - cx, y + 0.5 - cy
                dx, dy = (ux * ca + uy * sa) / rx, (-ux * sa + uy * ca) / ry
                d = dx * dx + dy * dy
                if d > 1 or (clip and not clip(x, y)):
                    continue
                # shadow: a circle offset towards the top-left cuts out the lit part
                sx, sy = dx + 0.28, dy + 0.30
                c = base if sx * sx + sy * sy < 1.0 - shadow * 0.3 else dark
                if glint:
                    gx, gy = ux / max(rx, ry) + 0.42, uy / max(rx, ry) + 0.42
                    gd = gx * gx + gy * gy
                    if gd < 0.05:
                        c = mix(light, (255, 255, 255), 0.75)
                    elif gd < 0.13 and gx * 1 + gy * 1 < 0.05:
                        c = light
                self._put(x, y, c, m)
        return self

    def pixels(self, pts, mat, t):
        m = M[mat]
        self.piece += 1
        for (x, y) in pts:
            self._put(x, y, m.at(t), m)
        self.piece_flat = self.piece
        return self

    def rows(self, rows, legend, ox=0, oy=0):
        """Hand-placed pixels: char -> (mat, t)."""
        self.piece += 1
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in legend:
                    mat, t = legend[ch]
                    self._put(ox + x, oy + y, M[mat].at(t), M[mat])
        return self

    def render(self, inner=True):
        """1px outline drawn OUTSIDE the shapes (keeps the soft fill intact); a piece lying on top of a
        different material gets a soft darker edge on its lower/right side."""
        img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
        p = img.load()
        for (x, y), (rgb, ol, piece) in self.px.items():
            c = rgb
            if inner:
                for dx, dy in ((0, 1), (1, 0)):
                    q = self.px.get((x + dx, y + dy))
                    if q and q[2] < piece and q[1] != ol:
                        c = mix(rgb, ol, 0.35)
                        break
            p[x, y] = c + (255,)
        # outside outline
        outl = {}
        for (x, y), (rgb, ol, piece) in self.px.items():
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                n = (x + dx, y + dy)
                if 0 <= n[0] < 16 and 0 <= n[1] < 16 and n not in self.px:
                    if n not in outl or piece > outl[n][1]:
                        outl[n] = (ol, piece)
        for (x, y), (ol, _) in outl.items():
            p[x, y] = ol + (255,)
        return img


def sheet(images, path, scale=8):
    w = 16 * scale + 10
    c = Image.new('RGBA', (len(images) * w, w), (57, 52, 49, 255))
    for i, im in enumerate(images):
        c.paste(Image.new('RGBA', (16 * scale, 16 * scale), (139, 139, 139, 255)), (i * w + 5, 5))
        b = im.resize((16 * scale, 16 * scale), Image.NEAREST)
        c.paste(b, (i * w + 5, 5), b)
    c.save(path)
