"""Cozy Cookery art kit v2.

Style: chunky, readable sprites that sit next to the current vanilla (Jappa) textures.
- Every material is a hue-shifted ramp of 6 colours: 0 = outline, 1-2 = shadow, 3 = base, 4 = light, 5 = shine.
  Shadows lean cool/purple, lights lean warm/yellow (like vanilla 1.14+).
- Light comes from the top-left. Outlines sit on the sprite's own edge pixels (inner outline),
  in the darkest colour of that material, never pure black.
- Little or no noise: flat shapes, 2-3 tones each, one shine pixel where it reads.
"""
import math, random
from PIL import Image


def hx(s):
    s = s.lstrip('#')
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4))


def ramp(*cols):
    return [hx(c) for c in cols]


# ---------- palette ----------
R = {
    # metals
    'iron':    ramp('#262734', '#4a4d5e', '#727789', '#a2a8b6', '#cfd4dc', '#f4f6f8'),
    'gold':    ramp('#4a2a0c', '#8a5310', '#c98a16', '#ecbb2c', '#fbe36a', '#fff8c0'),
    'diamond': ramp('#0e3a3c', '#14686a', '#1fa3a0', '#3fd3c8', '#8ef0e4', '#e2fffb'),
    'netherite': ramp('#1a1418', '#2e2529', '#463a3e', '#5e5054', '#7a6a6c', '#9c8c8a'),
    'flint':   ramp('#1a1a1e', '#2f2f36', '#46464f', '#5f6069', '#82838c', '#b2b3ba'),
    'copper':  ramp('#3e160e', '#79301c', '#b24f2c', '#d9733f', '#f0a067', '#ffd6a8'),
    # woods
    'oak':     ramp('#2f1c0e', '#5a3818', '#87592a', '#b07c3d', '#d2a35a', '#ecca86'),
    'darkwood': ramp('#1e1008', '#3a2210', '#5a3618', '#7a4e24', '#9a6a34', '#ba8a4c'),
    # ceramics
    'plate':   ramp('#5d4f48', '#a59a8e', '#d7cfc2', '#ece6da', '#f8f5ee', '#ffffff'),
    'glaze':   ramp('#4c1d14', '#86331f', '#b5502c', '#cf6c3c', '#e48d55', '#f6b88a'),
    'bowl':    ramp('#2a1608', '#4a2a10', '#6e4318', '#8f5c24', '#a87434', '#c08c48'),
    # produce
    'tomato':  ramp('#3e0a14', '#74121c', '#ad2020', '#d93a26', '#f16a40', '#ffc09a'),
    'leaf':    ramp('#12290f', '#244b16', '#3a721e', '#579a28', '#82c03a', '#bfe27a'),
    'cabbage': ramp('#173219', '#2f5f29', '#4d8a3a', '#72b04f', '#a0d071', '#d6efa8'),
    'onion':   ramp('#3a1414', '#6e2a1c', '#a2482a', '#c9703a', '#e39a56', '#f7c98c'),
    'onionflesh': ramp('#4e3a52', '#8a7096', '#bba6c4', '#dcccdf', '#f0e6f0', '#ffffff'),
    'garlic':  ramp('#4a3e3a', '#8a7c72', '#bfb2a4', '#e0d6c8', '#f2ece2', '#ffffff'),
    'carrot':  ramp('#4a1c06', '#8a3a0c', '#c45e12', '#ea8220', '#f8aa3e', '#ffd88a'),
    'potato':  ramp('#3e2610', '#6e4a22', '#a07a3e', '#c6a05c', '#e2c482', '#f6e2b0'),
    'potflesh': ramp('#6a5a2c', '#a8964e', '#d6c47a', '#ece0a0', '#f8f0c8', '#ffffff'),
    'mushroom': ramp('#3a2418', '#634232', '#8e6650', '#b38c70', '#d2b294', '#eedcc4'),
    'apple':   ramp('#3a0a10', '#6e1018', '#a81c1e', '#d8352a', '#f06644', '#ffb08a'),
    'beet':    ramp('#2a0818', '#4e0f2c', '#7a1a42', '#a42c58', '#c84e76', '#e88aa6'),
    'pumpkin': ramp('#4a1c04', '#8a3c08', '#c86410', '#e8881e', '#f8b040', '#ffdc8a'),
    # meat
    'rawbeef': ramp('#3c0c14', '#6e1620', '#a8282c', '#cf4242', '#e66e66', '#f6a8a0'),
    'rawpork': ramp('#5a2228', '#994450', '#cc6e76', '#e8949a', '#f6bcbe', '#ffe0e0'),
    'rawmutton': ramp('#3e0e12', '#701c20', '#a0322e', '#c44c42', '#dc7464', '#f0a898'),
    'rawchicken': ramp('#6a3a36', '#b06a62', '#dc9a8e', '#f0bcae', '#fad8cc', '#fff0ea'),
    'fat':     ramp('#7a6a62', '#c2b2a6', '#e6dacc', '#f4ece2', '#fcf8f2', '#ffffff'),
    'cooked':  ramp('#2c1406', '#5a2c0e', '#8a4818', '#b06628', '#d08a40', '#ecb468'),
    'crispy':  ramp('#2a1004', '#552008', '#86380e', '#b25a1c', '#d8822e', '#f2b052'),
    'bone':    ramp('#5a5044', '#9a8e7e', '#cfc4b2', '#e8e0d0', '#f6f2e8', '#ffffff'),
    # doughs & grains
    'dough':   ramp('#6a5038', '#a88a64', '#d6bc92', '#ead6b0', '#f6ead0', '#fffaf0'),
    'bread':   ramp('#3e1e08', '#6e3a12', '#a8601e', '#cc8430', '#e8aa4c', '#f8d084'),
    'crumb':   ramp('#7a6040', '#b89c6c', '#e2cc98', '#f2e2b8', '#faf2da', '#ffffff'),
    'flour':   ramp('#8a8070', '#c2b8a6', '#e2dacb', '#f0eadf', '#faf7f0', '#ffffff'),
    'pasta':   ramp('#6a4a10', '#a8801e', '#d8b040', '#f0cc5e', '#fae288', '#fff4c0'),
    'egg':     ramp('#8a6a10', '#c89a12', '#f0be1c', '#fad44a', '#ffe888', '#fff8d0'),
    # sauces & liquids
    'sauce':   ramp('#3a0808', '#6a1210', '#9e2214', '#c63c1c', '#e2602e', '#f6965a'),
    'stew':    ramp('#2a1206', '#4e2410', '#74381a', '#965024', '#b46e36', '#d0904e'),
    'broth':   ramp('#5a3a0c', '#8e6418', '#c08e2a', '#dcae46', '#f0cc6e', '#fae6a4'),
    'water':   ramp('#14234e', '#1e3a7a', '#2c56a8', '#3e72c8', '#5a92e0', '#8cb8f2'),
    'char':    ramp('#0c0a0a', '#1a1614', '#2a2420', '#3c332c', '#504438', '#6a5a4a'),
    'brick':   ramp('#2e1210', '#57221a', '#7e3426', '#9c4632', '#b65c42', '#cc7a5c'),
    'mortar':  ramp('#3c3834', '#66605a', '#8c857c', '#aaa298', '#c4bcb0', '#dcd6cc'),
    'twine':   ramp('#3a2a14', '#6a5028', '#9a7a42', '#c0a060', '#dcc282', '#f2e2b0'),
}


class Sprite:
    """16x16 canvas made of (material, level) pixels; rendered + outlined at the end."""

    def __init__(self, w=16, h=16, seed=1):
        self.w, self.h = w, h
        self.px = {}           # (x,y) -> [mat, level, layer]
        self.layer = 0
        self.rnd = random.Random(seed)

    # --- raw put ---
    def put(self, x, y, mat, lvl):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[(x, y)] = [mat, max(0, min(5, lvl)), self.layer]

    def new_layer(self):
        self.layer += 1
        return self

    # --- shapes ---
    def ball(self, cx, cy, rx, ry, mat, base=3, spread=1.6, noise=0.0, shine=True, clip=None):
        """Shaded ellipse, light from the top-left."""
        self.new_layer()
        lx, ly, lz = -0.55, -0.65, 0.52
        best = None
        for y in range(self.h):
            for x in range(self.w):
                dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
                d = dx * dx + dy * dy
                if d > 1 or (clip and not clip(x, y)):
                    continue
                nz = math.sqrt(max(0.0, 1 - d))
                l = dx * lx + dy * ly + nz * lz
                v = base + (l - 0.45) * spread * 2.2
                if noise:
                    v += self.rnd.uniform(-noise, noise)
                lvl = int(round(v))
                lvl = max(1, min(4, lvl))
                self.put(x, y, mat, lvl)
                if best is None or l > best[0]:
                    best = (l, x, y)
        if shine and best:
            self.put(best[1], best[2], mat, 5)
        return self

    def rect(self, x0, y0, x1, y1, mat, lvl=3, bevel=True):
        """Inclusive rectangle; top/left edge lighter, bottom/right darker."""
        self.new_layer()
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                v = lvl
                if bevel:
                    if y == y0 or x == x0:
                        v = lvl + 1
                    if y == y1 or x == x1:
                        v = lvl - 1
                self.put(x, y, mat, v)
        return self

    def cube(self, x, y, s, mat, top_mat=None):
        """Tiny isometric-ish cube (diced food): light top, base front, dark side."""
        self.new_layer()
        top_mat = top_mat or mat
        for i in range(s):
            for j in range(s):
                self.put(x + i, y + j, mat, 3)
        for i in range(s):
            self.put(x + i, y, top_mat, 4)
        self.put(x, y, top_mat, 5)
        for j in range(1, s):
            self.put(x + s - 1, y + j, mat, 2)
        return self

    def grid(self, rows, legend, ox=0, oy=0):
        """Hand-pixelled art: rows of chars, legend char -> (mat, level)."""
        self.new_layer()
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in legend:
                    m, l = legend[ch]
                    self.put(ox + x, oy + y, m, l)
        return self

    def line(self, pts, mat, lvl):
        self.new_layer()
        for (x, y) in pts:
            self.put(x, y, mat, lvl)
        return self

    def erase(self, pts):
        for p in pts:
            self.px.pop(p, None)
        return self

    # --- render ---
    def render(self, outline=True, inner=True):
        img = Image.new('RGBA', (self.w, self.h), (0, 0, 0, 0))
        p = img.load()
        out = {}
        for (x, y), (m, l, layer) in self.px.items():
            lvl = l
            if outline:
                edge = any((x + dx, y + dy) not in self.px for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if edge:
                    lvl = 0
                elif inner:
                    # where a lower layer meets a higher layer below/right of it -> one step darker
                    for dx, dy in ((1, 0), (0, 1)):
                        q = self.px.get((x + dx, y + dy))
                        if q and q[2] > layer and q[0] != m:
                            lvl = max(1, l - 1)
            out[(x, y)] = R[m][lvl]
        for (x, y), c in out.items():
            p[x, y] = c + (255,)
        return img


def sheet(images, path, scale=6, cols=8, bg=(57, 52, 49)):
    """Preview sheet (with a vanilla-ish slot behind every sprite)."""
    cell = 16 * scale + 12
    rows = (len(images) + cols - 1) // cols
    c = Image.new('RGBA', (cols * cell, rows * cell), bg + (255,))
    for i, im in enumerate(images):
        x, y = (i % cols) * cell + 6, (i // cols) * cell + 6
        slot = Image.new('RGBA', (16 * scale, 16 * scale), (139, 139, 139, 255))
        c.paste(slot, (x, y))
        big = im.resize((im.width * scale, im.height * scale), Image.NEAREST)
        c.paste(big, (x, y), big)
    c.save(path)
