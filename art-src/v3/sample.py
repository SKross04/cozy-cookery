import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from soft import Pic, sheet, M
from PIL import Image

OUT = sys.argv[1] if len(sys.argv) > 1 else '/tmp/v5'
os.makedirs(OUT, exist_ok=True)


def tomato():
    p = Pic()
    p.ball(8, 9.8, 5.8, 5.2, 'tomato', ang=-0.1)
    p.rows([
        "....d....",
        "...dm....",
        "dm.dm.mmd",
        ".dmlmdml.",
        "mdlmdmldm",
        ".m.d.d.m.",
    ], {'d': ('leaf', 0.0), 'm': ('leaf', 0.5), 'l': ('leaf', 0.75)}, ox=4, oy=1)
    return p.render(inner=False)


def cabbage():
    p = Pic()
    p.ball(5, 9.4, 3.6, 4.4, 'cabbage_dark', shine=False)
    p.ball(11, 9.4, 3.6, 4.4, 'cabbage_dark', shine=False)
    p.ball(8, 8.6, 4.6, 5.0, 'cabbage')
    p.pixels([(8, 6), (8, 7), (8, 8), (8, 9), (8, 10), (7, 8), (9, 8), (6, 10), (10, 10)], 'cabbage', 1.0)
    return p.render()


def onion():
    p = Pic()
    p.ball(8, 9.4, 5.4, 4.8, 'onion', stripes=3, stripe_amp=0.2, ang=-0.35)
    p.rows(["..a", ".a.", "a.."], {'a': ('onionskin', 0.5)}, ox=8, oy=2)
    p.rows(["a.a.a"], {'a': ('onionskin', 0.2)}, ox=6, oy=14)
    return p.render()


def garlic():
    p = Pic()
    p.ball(8, 9.6, 5.2, 4.4, 'garlic', stripes=3, stripe_amp=0.2, ang=-0.3)
    p.rows([".a.", "aa.", "a.."], {'a': ('garlic', 0.7)}, ox=9, oy=3)
    p.pixels([(6, 12), (7, 12), (10, 12)], 'garlicpink', 0.5)
    return p.render()


def knife():
    p = Pic()
    blade = []
    for y in range(16):
        for x in range(16):
            a, q = x - y, x + y
            if -2 <= a <= 11 and 14 <= q <= 17 and not (a >= 8 and q < 14 + (a - 7)):
                blade.append((x, y, q))
    p.piece += 1
    for (x, y, q) in blade:
        p._put(x, y, M['iron'].at({14: 0.85, 15: 0.6, 16: 0.35, 17: 1.0}[q]), M['iron'])
    p.piece += 1
    for y in range(16):
        for x in range(16):
            a, q = x - y, x + y
            if -11 <= a <= -4 and 15 <= q <= 17:
                p._put(x, y, M['handle'].at({15: 0.85, 16: 0.55, 17: 0.2}[q]), M['handle'])
    for (x, y) in [(5, 9), (6, 9), (6, 10), (7, 10)]:
        p._put(x, y, M['iron'].at(0.5), M['iron'])
    return p.render(inner=False)


def bowl_dish(food):
    p = Pic()
    p.ball(8, 9.8, 6.6, 4.6, 'bowl', shine=False)
    p.piece += 1
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 6.0, (y + 0.5 - 8.2) / 3.0
            if 0.62 < dx * dx + dy * dy <= 1.0:
                p._put(x, y, M['bowl'].at(0.95 if x < 9 else 0.75), M['bowl'])
    food(p)
    return p.render()


def stew_food(p):
    p.ball(8, 8.2, 4.8, 2.1, 'stew', shine=False)
    p.ball(5.4, 7.3, 1.8, 1.5, 'carrot')
    p.ball(9.2, 6.8, 2.0, 1.6, 'beef')
    p.ball(11.0, 8.2, 1.6, 1.3, 'potato')
    p.ball(7.4, 8.8, 1.6, 1.2, 'potato', shine=False)
    p.pixels([(7, 6), (10, 9), (6, 9)], 'leaf', 0.8)


def plate_dish(food):
    p = Pic()
    p.ball(8, 10.6, 7.0, 3.8, 'plate', shine=False, lo=0.4)
    p.piece += 1
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 7.0, (y + 0.5 - 10.6) / 3.8
            if 0.6 < dx * dx + dy * dy <= 0.8:
                p._put(x, y, M['rim'].at(0.6), M['plate'])
    food(p)
    return p.render()


def bolognese(p):
    p.ball(8, 8.6, 4.8, 3.2, 'pasta', stripes=4, stripe_amp=0.1)
    p.ball(8.3, 7.2, 2.8, 1.8, 'sauce')
    p.pixels([(9, 5), (10, 5)], 'leaf', 0.8)


def drumstick():
    p = Pic()
    p.rows([
        "aa....",
        "aab...",
        ".bbb..",
        "..bbab",
        "...bba",
        "...ab.",
    ], {'a': ('bone', 0.95), 'b': ('bone', 0.55)}, ox=9, oy=8)
    p.ball(9.2, 9.2, 2.6, 2.4, 'cooked', shine=False)
    p.ball(6.4, 6.6, 5.0, 4.6, 'cooked', ang=0.6)
    return p.render()


imgs = {'iron_knife': knife(), 'tomato': tomato(), 'cabbage': cabbage(), 'onion': onion(), 'garlic': garlic(),
        'beef_stew': bowl_dish(stew_food), 'spaghetti_bolognese': plate_dish(bolognese),
        'cooked_chicken_drumstick': drumstick()}
for k, im in imgs.items():
    im.save(f'{OUT}/{k}.png')
refs = [Image.open('/mnt/user-data/uploads/' + f).convert('RGBA') for f in ('01afe13b-image.png', 'b263e80e-image.png')]
sheet(refs + list(imgs.values()), f'{OUT}/_sheet.png')
print('ok')
