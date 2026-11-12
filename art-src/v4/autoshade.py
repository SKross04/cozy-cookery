"""Vanilla-style auto shader: draw shapes as masks, get vanilla-like shading.

Rules (taken from vanilla 1.14+ items like the apple, potato and beef):
- outline outside the shape: darkest colour on the bottom/right side, a lighter dark on the top/left side
- inside: top/left edge = light, bottom/right edge = shade, middle = base with a little pixel texture
- a small highlight cluster near the top-left
Palettes have 6 colours: o (darkest), d (dark), s (shade), m (base), l (light), h (highlight).
"""
import math
from PIL import Image


def hx(s):
    s = s.lstrip('#')
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def pal(*cols):
    return dict(zip('odsmlh', [hx(c) for c in cols]))


class Piece:
    def __init__(self, mask, p, outline=True, tex=True, shine=True, flat=None):
        self.mask = set((x, y) for (x, y) in mask if 0 <= x < 16 and 0 <= y < 16)
        self.p = p
        self.outline = outline
        self.tex = tex
        self.shine = shine
        self.flat = flat      # force one colour key for the whole piece (e.g. 'm')
        self.extra = {}       # (x,y) -> rgba drawn after shading

    def at(self, pts, col):
        for q in pts:
            if q in self.mask:
                self.extra[q] = hx(col) if isinstance(col, str) else col
        return self


# ---------- masks ----------
def ellipse(cx, cy, rx, ry, ang=0.0):
    out = []
    ca, sa = math.cos(ang), math.sin(ang)
    for y in range(16):
        for x in range(16):
            ux, uy = x + 0.5 - cx, y + 0.5 - cy
            dx, dy = (ux * ca + uy * sa) / rx, (-ux * sa + uy * ca) / ry
            if dx * dx + dy * dy <= 1:
                out.append((x, y))
    return out


def rect(x0, y0, x1, y1):
    return [(x, y) for y in range(y0, y1 + 1) for x in range(x0, x1 + 1)]


def grid(rows, ch='#', ox=0, oy=0):
    return [(ox + x, oy + y) for y, r in enumerate(rows) for x, c in enumerate(r) if c == ch]


def band(p0, p1, a0, a1):
    """Diagonal band from bottom-left to top-right: x+y in [p0,p1], x-y in [a0,a1]."""
    return [(x, y) for y in range(16) for x in range(16) if p0 <= x + y <= p1 and a0 <= x - y <= a1]


def minus(a, b):
    b = set(b)
    return [q for q in a if q not in b]


# ---------- render ----------
def render(pieces):
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    px = img.load()
    for pc in pieces:
        m, p = pc.mask, pc.p
        if pc.outline:
            for (x, y) in m:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    q = (x + dx, y + dy)
                    if q in m or not (0 <= q[0] < 16 and 0 <= q[1] < 16):
                        continue
                    dark_side = dx > 0 or dy > 0
                    # don't let a lighter top/left outline overwrite a darkest one already there
                    cur = px[q]
                    col = p['o'] if dark_side else p['d']
                    if not dark_side and cur == p['o']:
                        continue
                    px[q] = col
        interior = [q for q in m if all((q[0] + dx, q[1] + dy) in m for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
        top = sorted(interior, key=lambda q: (q[0] + q[1], q[0]))[:2] if pc.shine else []
        for (x, y) in m:
            if pc.flat:
                px[x, y] = p[pc.flat]
                continue
            up, lf = (x, y - 1) not in m, (x - 1, y) not in m
            dn, rt = (x, y + 1) not in m, (x + 1, y) not in m
            if dn or rt:
                k = 's'
            elif up or lf:
                k = 'l'
            else:
                k = 'm'
                if pc.tex:
                    h = (x * 3 + y * 5) % 7
                    if h == 0:
                        k = 'l' if (x + y) < 15 else 's'
            if (x, y) in top:
                k = 'h'
            px[x, y] = p[k]
        for q, c in pc.extra.items():
            px[q] = c
    return img
