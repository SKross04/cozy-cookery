import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from kit import Sprite, sheet

OUT = sys.argv[1] if len(sys.argv) > 1 else '/tmp/v2'
os.makedirs(OUT, exist_ok=True)


KNIFE = [
    "..............oo",
    ".............owo",
    "............owlo",
    "...........owlmo",
    "..........owlmdo",
    ".........owlmdo.",
    "........owlmdo..",
    ".......owlmdo...",
    "......oBbbdo....",
    ".....OBbbbo.....",
    "....OkhHO.......",
    "...OkrHO........",
    "..OkhHO.........",
    ".OkrHO..........",
    "OkhHO...........",
    "OOOO............",
]


def knife(metal, handle='darkwood', rivet='iron', bolster='iron'):
    s = Sprite()
    s.grid(KNIFE, {'o': (metal, 0), 'w': (metal, 5), 'l': (metal, 4), 'm': (metal, 3), 'd': (metal, 2),
                   'B': (bolster, 4), 'b': (bolster, 2), 'O': (handle, 0), 'k': (handle, 4),
                   'h': (handle, 3), 'H': (handle, 2), 'r': (rivet, 5)})
    return s.render(outline=False)


def tomato():
    s = Sprite(seed=3)
    s.ball(8, 9.4, 7.0, 6.4, 'tomato', spread=1.4)
    s.grid([
        "..x.x..",
        ".xxxxx.",
        "x.xxx.x",
    ], {'x': ('leaf', 3)}, ox=5, oy=2)
    s.grid(["x", "x"], {'x': ('leaf', 2)}, ox=8, oy=0)
    return s.render()


def cabbage():
    s = Sprite(seed=4)
    s.ball(4.6, 9.6, 4.6, 5.6, 'cabbage', base=2.4, spread=1.0, shine=False)
    s.ball(11.4, 9.6, 4.6, 5.6, 'cabbage', base=2.4, spread=1.0, shine=False)
    s.ball(8, 8.2, 6.0, 6.2, 'cabbage', base=3.2, spread=1.2, shine=False)
    s.grid([
        "....x....",
        "....x....",
        ".x..x..x.",
        "..x.x.x..",
        "...xxx...",
        "....x....",
        "....x....",
    ], {'x': ('cabbage', 5)}, ox=4, oy=4)
    s.new_layer()
    for (x, y) in [(3, 9), (4, 11), (12, 9), (11, 11)]:
        s.put(x, y, 'cabbage', 4)
    return s.render()


def onion():
    s = Sprite(seed=5)
    s.ball(8, 9.6, 6.6, 5.8, 'onion', spread=1.3)
    s.grid(["..x", ".xx", "xx."], {'x': ('onion', 3)}, ox=7, oy=1)
    s.grid([".x.x."], {'x': ('twine', 2)}, ox=6, oy=15)
    s.new_layer()
    for (x, y) in [(6, 8), (6, 9), (7, 11), (10, 8), (10, 9), (10, 10)]:
        s.put(x, y, 'onion', 2)
    return s.render()


def garlic():
    s = Sprite(seed=6)
    s.ball(8, 9.8, 6.6, 5.4, 'garlic', spread=1.1)
    s.grid(["..x..", ".xxx.", ".xxx.", "xxxxx"], {'x': ('garlic', 3)}, ox=6, oy=3)
    s.erase([(8, 2)])
    s.new_layer()
    for (x, y) in [(6, 7), (5, 8), (5, 9), (5, 10), (5, 11), (10, 7), (11, 8), (11, 9), (11, 10), (11, 11), (8, 12), (8, 11)]:
        s.put(x, y, 'garlic', 2)
    for (x, y) in [(7, 9), (9, 10)]:
        s.put(x, y, 'onionflesh', 2)
    s.grid([".x.x.x."], {'x': ('garlic', 1)}, ox=5, oy=15)
    return s.render()


CUBE = [
    ".ooooo.",
    "owtttso",
    "oFFFFso",
    "oFmFFso",
    "oFFmFso",
    "oFFFFso",
    ".ooooo.",
]


def cubes(mat, fat='fat', positions=((1, 1), (8, 2), (2, 8), (9, 9))):
    s = Sprite(seed=7)
    for (x, y) in positions:
        s.grid(CUBE, {'o': (mat, 0), 'w': (mat, 5), 't': (mat, 4), 'F': (mat, 3), 's': (mat, 2),
                      'm': (fat, 3)}, ox=x, oy=y)
    return s.render(outline=False)


BOWL_BODY = {8: (1, 14), 9: (1, 14), 10: (2, 13), 11: (2, 13), 12: (3, 12), 13: (4, 11)}
SOUP = {5: (3, 12), 6: (2, 13), 7: (1, 14)}


def bowl(content, chunks):
    s = Sprite(seed=8)
    s.new_layer()
    for y, (a, b) in SOUP.items():
        for x in range(a, b + 1):
            s.put(x, y, content, 4 if y == 6 and x < 8 else 3)
    s.new_layer()
    for y, (a, b) in BOWL_BODY.items():
        for x in range(a, b + 1):
            lvl = 4 if x < a + 3 else (2 if x > b - 3 else 3)
            if y == 8:
                lvl = 5 if x < 8 else 4
            s.put(x, y, 'bowl', lvl)
    s.new_layer()
    for (x, y), m in chunks.items():
        s.put(x, y, m, 4)
    return s.render()


def plate(fill):
    s = Sprite(seed=9)
    s.ball(8, 9, 7.8, 5.6, 'plate', base=3.2, spread=0.8, shine=False)
    s.new_layer()
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 7.8, (y + 0.5 - 9) / 5.6
            if 0.62 < dx * dx + dy * dy <= 0.78:
                s.put(x, y, 'glaze', 3)
    s.ball(8, 9, 5.2, 3.5, 'plate', base=2.6, spread=0.4, shine=False)
    fill(s)
    return s.render()


def bolognese(s):
    s.ball(8, 8.2, 4.8, 3.4, 'pasta', base=3, spread=1.0, shine=False)
    s.new_layer()
    for (x, y) in [(5, 8), (6, 7), (9, 10), (10, 9), (11, 8), (6, 10), (7, 10)]:
        s.put(x, y, 'pasta', 2)
    s.ball(8.3, 7.4, 3.0, 2.0, 'sauce', base=3, spread=1.0)
    s.new_layer()
    s.put(9, 6, 'leaf', 4)
    s.put(10, 6, 'leaf', 3)
    s.put(7, 7, 'stew', 2)
    s.put(9, 8, 'stew', 2)


def bread_slice():
    s = Sprite(seed=10)
    s.grid([
        "...xxxxxx...",
        "..xxxxxxxx..",
        ".xxxxxxxxxx.",
        ".xxxxxxxxxx.",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
        "..xxxxxxxx..",
    ], {'x': ('bread', 3)}, ox=2, oy=2)
    s.grid([
        "....yyyy....",
        "...yyyyyy...",
        "..yyyyyyyy..",
        "..yyyyyyyy..",
        "...yyyyyy...",
        "...yyyyyy...",
        "...yyyyyy...",
        "...yyyyyy...",
        "...yyyyyy...",
        "...yyyyyy...",
    ], {'y': ('crumb', 3)}, ox=2, oy=3)
    s.new_layer()
    for (x, y) in [(6, 5), (9, 7), (7, 10), (10, 11), (6, 12)]:
        s.put(x, y, 'crumb', 1)
    for (x, y) in [(6, 4), (5, 5)]:
        s.put(x, y, 'crumb', 5)
    return s.render()


def drumstick():
    s = Sprite(seed=11)
    s.ball(6.4, 6.4, 6.0, 5.6, 'cooked', spread=1.5)
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
    s.new_layer()
    for (x, y) in [(4, 5), (7, 4), (5, 8), (8, 7), (3, 8)]:
        s.put(x, y, 'crispy', 2)
    return s.render()


imgs = {
    'iron_knife': knife('iron'),
    'diamond_knife': knife('diamond'),
    'tomato': tomato(),
    'cabbage': cabbage(),
    'onion': onion(),
    'garlic': garlic(),
    'raw_beef_cubes': cubes('rawbeef'),
    'potato_cubes': cubes('potflesh', fat='potflesh'),
    'beef_stew': bowl('stew', {(4, 6): 'carrot', (9, 6): 'cooked', (11, 7): 'potato', (6, 7): 'carrot', (7, 5): 'leaf'}),
    'spaghetti_bolognese_plate': plate(bolognese),
    'bread_slice': bread_slice(),
    'cooked_chicken_drumstick': drumstick(),
}
for k, im in imgs.items():
    im.save(f'{OUT}/{k}.png')
sheet(list(imgs.values()), f'{OUT}/_sheet.png', scale=6, cols=6)
print('ok')
