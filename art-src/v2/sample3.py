"""Sample batch 3: 3/4 top-down food in the spirit of Farmer's Delight / Let's Do (textured, heaped, own drawings)."""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from kit import Sprite, sheet
import sample as old   # knife + cube grids

OUT = sys.argv[1] if len(sys.argv) > 1 else '/tmp/v4'
os.makedirs(OUT, exist_ok=True)


def wooden_bowl(s, food_mat):
    s.blob(8, 10.2, 7.5, 5.3, 'bowl', base=2.6, spread=1.0, wobble=0, tex=0.25, shine=False)
    s.new_layer()
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 6.3, (y + 0.5 - 8.6) / 3.5
            if dx * dx + dy * dy <= 1:
                s.put(x, y, 'bowl', 1)
            elif dx * dx + dy * dy <= 1.45 and y < 9:
                s.put(x, y, 'bowl', 4 if x < 9 else 3)
    s.blob(8, 8.3, 5.7, 3.0, food_mat, base=3, spread=0.8, wobble=0.05, tex=0.5, shine=False)


def beef_stew():
    s = Sprite(seed=21)
    wooden_bowl(s, 'stew')
    s.blob(5.2, 7.2, 1.9, 1.5, 'carrot', base=3.2, tex=0.2, seed=1)
    s.blob(9.4, 6.6, 2.2, 1.8, 'cooked', base=3, tex=0.3, seed=2)
    s.blob(11.6, 8.6, 1.7, 1.4, 'potflesh', base=3, tex=0.2, seed=3)
    s.blob(7.4, 9.4, 1.9, 1.4, 'cooked', base=2.8, tex=0.3, seed=4)
    s.sprinkle([(7, 6), (8, 6), (10, 9), (4, 9)], 'leaf', (4, 3))
    return s.render()


def plate_bolognese():
    s = Sprite(seed=22)
    s.blob(8, 11, 7.6, 4.4, 'plate', base=3.2, spread=0.6, wobble=0, tex=0.1, shine=False)
    s.new_layer()
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 7.6, (y + 0.5 - 11) / 4.4
            if 0.6 < dx * dx + dy * dy <= 0.78:
                s.put(x, y, 'glaze', 3 if y > 11 else 4)
    s.blob(8, 8.8, 5.3, 3.6, 'pasta', base=3, spread=1.0, wobble=0.12, tex=0.6, shine=False, seed=5)
    s.sprinkle([(4, 9), (5, 8), (6, 10), (10, 10), (11, 9), (12, 8), (5, 10), (9, 11), (7, 11), (11, 11)], 'pasta', (2, 4))
    s.blob(8.4, 7.4, 3.4, 2.2, 'sauce', base=3, spread=1.2, wobble=0.15, tex=0.5, seed=6)
    s.sprinkle([(7, 7), (9, 8), (10, 6)], 'stew', (2, 3))
    s.sprinkle([(9, 5), (10, 5), (6, 6)], 'leaf', (4, 3))
    return s.render()


def tomato():
    s = Sprite(seed=23)
    s.blob(8, 9.6, 6.7, 6.0, 'tomato', base=3, spread=1.4, wobble=0.06, tex=0.3, seed=7)
    s.grid([
        "....x.x...",
        ".xx.xxx.xx",
        "..xxxxxxx.",
        ".xxxxxxxxx",
        "xx..xxx..x",
        "x....x....",
    ], {'x': ('leaf', 3)}, ox=3, oy=2)
    s.sprinkle([(8, 3), (6, 4), (10, 4)], 'leaf', (4,))
    s.grid(["x.", "x."], {'x': ('leaf', 2)}, ox=8, oy=0)
    return s.render()


def cabbage():
    s = Sprite(seed=24)
    s.blob(4.4, 9.8, 4.4, 5.4, 'cabbage', base=2.4, spread=1.0, tex=0.4, shine=False, seed=8)
    s.blob(11.6, 9.8, 4.4, 5.4, 'cabbage', base=2.4, spread=1.0, tex=0.4, shine=False, seed=9)
    s.blob(8, 12.4, 5.6, 3.4, 'cabbage', base=2.6, spread=1.0, tex=0.4, shine=False, seed=10)
    s.blob(8, 7.8, 5.2, 5.0, 'cabbage', base=3.4, spread=1.1, tex=0.35, seed=11)
    s.sprinkle([(8, 5), (8, 6), (8, 7), (7, 8), (9, 8), (6, 9), (10, 9), (3, 9), (4, 11), (12, 9), (11, 11),
                (8, 12), (7, 13)], 'cabbage', (5,))
    return s.render()


def onion():
    s = Sprite(seed=25)
    s.blob(8, 9.8, 6.5, 5.7, 'onion', base=3, spread=1.3, wobble=0.05, tex=0.35, seed=12)
    s.grid(["...x", "..xx", ".xx.", "xx.."], {'x': ('onion', 3)}, ox=6, oy=1)
    s.sprinkle([(5, 7), (5, 8), (5, 9), (6, 11), (10, 7), (11, 8), (11, 9), (10, 11), (8, 6), (8, 7)], 'onion', (2,))
    s.grid([".x.x.x."], {'x': ('twine', 2)}, ox=5, oy=15)
    return s.render()


def garlic():
    s = Sprite(seed=26)
    s.blob(8, 10, 6.4, 5.3, 'garlic', base=3, spread=1.1, wobble=0.08, tex=0.25, seed=13)
    s.grid(["..x..", ".xxx.", ".xxx.", "xxxxx"], {'x': ('garlic', 3)}, ox=6, oy=2)
    s.erase([(8, 1)])
    s.sprinkle([(5, 8), (4, 9), (4, 10), (5, 12), (11, 8), (12, 9), (12, 10), (11, 12), (8, 9), (8, 10), (8, 11)],
               'garlic', (2,))
    s.sprinkle([(6, 9), (10, 10), (7, 12)], 'onionflesh', (2,))
    s.grid([".x.x.x."], {'x': ('garlic', 1)}, ox=5, oy=15)
    return s.render()


def drumstick():
    s = Sprite(seed=27)
    s.blob(6.4, 6.6, 6.0, 5.6, 'cooked', base=3, spread=1.5, wobble=0.1, tex=0.55, seed=14)
    s.grid([
        "xx.....",
        "xxx....",
        ".xxx...",
        "..xxx.x",
        "...xxxx",
        "....xxx",
        "...xxx.",
        "....x..",
    ], {'x': ('bone', 4)}, ox=9, oy=8)
    s.sprinkle([(4, 4), (7, 3), (3, 8), (8, 8), (6, 6)], 'crispy', (2, 3))
    return s.render()


def beef_heap():
    s = Sprite(seed=28)
    for i, (x, y, rx, ry) in enumerate([(4.5, 9.5, 3.2, 2.6), (11, 10, 3.2, 2.6), (7.6, 12.2, 3.4, 2.6),
                                         (8, 7, 3.2, 2.6)]):
        s.blob(x, y, rx, ry, 'rawbeef', base=3, spread=1.3, wobble=0.18, tex=0.3, seed=30 + i)
        s.sprinkle([(int(x), int(y)), (int(x) + 1, int(y) - 1)], 'fat', (3, 4))
    return s.render()


imgs = {
    'iron_knife': old.knife('iron'),
    'tomato': tomato(),
    'cabbage': cabbage(),
    'onion': onion(),
    'garlic': garlic(),
    'raw_beef_cubes': beef_heap(),
    'beef_stew': beef_stew(),
    'spaghetti_bolognese_plate': plate_bolognese(),
    'cooked_chicken_drumstick': drumstick(),
}
for k, im in imgs.items():
    im.save(f'{OUT}/{k}.png')
sheet(list(imgs.values()), f'{OUT}/_sheet.png', scale=7, cols=9)
print('ok')
