"""Cookbook art: GUI book spread (256x256 texture, spread uses 256x180), item icon, block textures."""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from PIL import Image
from vanilla_style import draw, hx

A = sys.argv[1]  # assets/cozycookery/textures

# ---------- GUI: open book, leather cover + 2 parchment pages ----------
W, H = 256, 180
img = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
p = img.load()
LEATHER = [hx('#3a1c0e'), hx('#5a2e16'), hx('#7a4220'), hx('#94562a')]
PAPER = [hx('#d8c49a'), hx('#e8d8b0'), hx('#f4e8c8'), hx('#faf2dc')]
for y in range(H):
    for x in range(W):
        edge = min(x, y, W - 1 - x, H - 1 - y)
        if edge == 0:
            c = LEATHER[0]
        elif edge < 6:
            c = LEATHER[2] if (x + y) % 7 else LEATHER[3]
            if edge == 5:
                c = LEATHER[1]
        else:
            px = x - 6 if x < 128 else W - 7 - x       # distance from the outer page edge
            gutter = abs(x - 127.5)
            c = PAPER[2]
            if gutter < 2:
                c = LEATHER[1]
            elif gutter < 5:
                c = PAPER[0]
            elif gutter < 10:
                c = PAPER[1]
            elif px < 2 or y < 8 or y > H - 9:
                c = PAPER[1]
            if c == PAPER[2] and (x * 13 + y * 7) % 97 == 0:
                c = PAPER[3]
        p[x, y] = c
# stacked page edges at the bottom
for x in range(8, W - 8):
    if abs(x - 127.5) > 3:
        p[x, H - 7] = PAPER[0]
        p[x, H - 8] = PAPER[1]
img.save(f'{A}/gui/cookbook.png')

# ---------- item: closed red-brown cookbook with a golden tomato emblem ----------
item = draw([
    "................",
    "................",
    "...OOOOOOOOOOO..",
    "..OLLLLLLLLLLLO.",
    "..OLmmmmmmmmmmO.",
    "..OLmmmmGGmmmmO.",
    "..OLmmmGYYGmmmO.",
    "..OLmmGYyyYGmmO.",
    "..OLmmGYyyYGmmO.",
    "..OLmmmGYYGmmmO.",
    "..OLmmmmGGmmmdO.",
    "..OLmmmmmmmmddO.",
    "..OLddddddddddO.",
    "..OPpPpPpPpPpPO.",
    "...OOOOOOOOOOO..",
    "................",
], {'O': '#3a1406', 'L': '#e05a3a', 'm': '#c03a26', 'd': '#8a2214', 'G': '#8a5a0a', 'Y': '#f0c030',
    'y': '#fff080', 'P': '#f4e8c8', 'p': '#d8c49a'})
item.save(f'{A}/item/cookbook.png')

# ---------- block: cover (outside) + pages (top, with text lines) ----------
cover = Image.new('RGBA', (16, 16), hx('#c03a26'))
cp = cover.load()
for i in range(16):
    cp[i, 0] = cp[i, 15] = cp[0, i] = cp[15, i] = hx('#8a2214')
cover.save(f'{A}/block/cookbook_cover.png')
pages = Image.new('RGBA', (16, 16), PAPER[2])
pp = pages.load()
for y in range(16):
    for x in range(16):
        if x in (7, 8):
            pp[x, y] = PAPER[0]
        elif y in (3, 5, 7, 9, 11) and 2 <= x % 8 <= 6 and not (y == 11 and x % 8 > 4):
            pp[x, y] = hx('#8a7a5a')
        elif y == 13 and x in (3, 4, 11):
            pp[x, y] = hx('#c03a26')
pages.save(f'{A}/block/cookbook_pages.png')
side = Image.new('RGBA', (16, 16), PAPER[1])
sp = side.load()
for x in range(16):
    for y in range(16):
        if y % 2:
            sp[x, y] = PAPER[0]
side.save(f'{A}/block/cookbook_page_edges.png')
print('ok')
