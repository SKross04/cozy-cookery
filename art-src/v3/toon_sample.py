import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from soft import Pic, sheet, M, Flat, hx
from PIL import Image

OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)
# candy-bright 3-tone palettes: [shadow, base, light]
M.update({
    'tomato':  Flat(['#c8102e', '#ff3a2a', '#ff9a7a'], '#5a0a24'),
    'leaf':    Flat(['#178a1a', '#2fbf2a', '#8ef05a'], '#0a4a10'),
    'cabbage': Flat(['#3c9a2a', '#6cd03e', '#c8ff8a'], '#155010'),
    'cabbage_dark': Flat(['#227a1e', '#34a02a', '#6cd03e'], '#0e3e0c'),
    'onion':   Flat(['#c8561a', '#f08a2a', '#ffd27a'], '#5a1a08'),
    'onionskin': Flat(['#7a3410', '#b05a1c', '#e08a3a'], '#4a1a08'),
    'garlic':  Flat(['#d8a6c0', '#fff0f2', '#ffffff'], '#6a2a50'),
    'garlicpink': Flat(['#c04a8a', '#e070b0', '#ffb0d8'], '#6a2a50'),
    'iron':    Flat(['#7a8098', '#c4ccdc', '#ffffff'], '#22243a'),
    'handle':  Flat(['#8a3a10', '#c8661e', '#f0a050'], '#3a1406'),
    'bowl':    Flat(['#8a4416', '#c06a26', '#f0a050'], '#3a1606'),
    'stew':    Flat(['#8a2a0e', '#c04418', '#e86a30'], '#3a1006'),
    'carrot':  Flat(['#e8600a', '#ff9a1a', '#ffd070'], '#6a2006'),
    'beef':    Flat(['#7a2414', '#a83a22', '#d86a4a'], '#3a0a08'),
    'potato':  Flat(['#f0c040', '#ffe070', '#fff8c8'], '#7a4a10'),
    'plate':   Flat(['#c0cae0', '#ffffff', '#ffffff'], '#2e3458'),
    'rim':     Flat(['#ff5a3a', '#ff5a3a'], '#2e3458'),
    'pasta':   Flat(['#f0a810', '#ffd83a', '#fff8a0'], '#7a3e06'),
    'sauce':   Flat(['#c8102e', '#ff3a2a', '#ff9a7a'], '#5a0a24'),
    'cooked':  Flat(['#c0540e', '#f08a24', '#ffd08a'], '#4a1606'),
    'bone':    Flat(['#d8ccc0', '#ffffff', '#ffffff'], '#4a3a3a'),
})


def tomato():
    p = Pic()
    p.toon(8, 9.4, 6.6, 6.0, 'tomato')
    p.rows([
        ".....d....",
        "....dm....",
        ".mm.dm.mm.",
        "mllmmmmllm",
        ".dmmldmmd.",
        "..d.m..d..",
    ], {'d': ('leaf', 0.0), 'm': ('leaf', 0.5), 'l': ('leaf', 1.0)}, ox=3, oy=0)
    return p.render(inner=False)


def cabbage():
    p = Pic()
    p.toon(4.6, 9.6, 4.2, 5.2, 'cabbage_dark', glint=False)
    p.toon(11.4, 9.6, 4.2, 5.2, 'cabbage_dark', glint=False)
    p.toon(8, 8.6, 5.4, 5.6, 'cabbage')
    p.pixels([(8, 7), (8, 8), (8, 9), (8, 10), (8, 11), (7, 9), (6, 10), (9, 9), (10, 10)], 'cabbage', 1.0)
    return p.render()


def onion():
    p = Pic()
    p.toon(8, 9.6, 6.2, 5.6, 'onion', ang=-0.2)
    p.rows(["...a", "..aa", ".aa.", "aa.."], {'a': ('onionskin', 0.5)}, ox=7, oy=0)
    p.pixels([(5, 9), (5, 10), (5, 11), (10, 9), (10, 10), (11, 11)], 'onion', 0.0)
    p.rows(["a.a.a.a"], {'a': ('onionskin', 0.0)}, ox=5, oy=15)
    return p.render()


def garlic():
    p = Pic()
    p.toon(8, 10, 6.2, 5.2, 'garlic', ang=-0.2)
    p.rows(["..a", ".aa", "aa.", "a.."], {'a': ('garlic', 0.5)}, ox=7, oy=1)
    p.pixels([(5, 8), (5, 9), (5, 10), (6, 11), (10, 8), (11, 9), (11, 10), (10, 11), (8, 12), (8, 13)], 'garlic', 0.0)
    p.pixels([(6, 13), (7, 13), (9, 13)], 'garlicpink', 0.5)
    return p.render()


KNIFE = [
    "..............ll",
    ".............lwl",
    "............lwmm",
    "...........lwmmd",
    "..........lwmmd.",
    ".........lwmmd..",
    "........lwmmd...",
    ".......lwmmd....",
    "......bbbmd.....",
    ".....bbbbd......",
    "....HhHb........",
    "...HhHH.........",
    "..HhHH..........",
    ".HhHH...........",
    "HhHH............",
    "HH..............",
]


def knife():
    p = Pic()
    p.rows(KNIFE, {'l': ('iron', 1.0), 'w': ('iron', 1.0), 'm': ('iron', 0.5), 'd': ('iron', 0.0),
                   'b': ('iron', 0.0), 'H': ('handle', 0.5), 'h': ('handle', 1.0)})
    return p.render(inner=False)


def stew():
    p = Pic()
    p.toon(8, 9.8, 7.2, 5.0, 'bowl', glint=False)
    p.piece += 1
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 6.4, (y + 0.5 - 8.0) / 3.0
            if dx * dx + dy * dy <= 1:
                p._put(x, y, M['stew'].stops[1] if y < 9 else M['stew'].stops[0], M['stew'])
    p.toon(5.2, 7.2, 2.0, 1.7, 'carrot')
    p.toon(9.2, 6.6, 2.2, 1.9, 'beef')
    p.toon(11.6, 8.0, 1.8, 1.5, 'potato')
    p.toon(7.4, 8.8, 1.8, 1.4, 'potato')
    p.pixels([(7, 6), (10, 9), (4, 9)], 'leaf', 1.0)
    return p.render()


def bolognese():
    p = Pic()
    p.toon(8, 10.8, 7.4, 4.0, 'plate', glint=False)
    p.piece += 1
    for y in range(16):
        for x in range(16):
            dx, dy = (x + 0.5 - 8) / 7.4, (y + 0.5 - 10.8) / 4.0
            if 0.62 < dx * dx + dy * dy <= 0.82:
                p._put(x, y, M['rim'].stops[0], M['plate'])
    p.toon(8, 8.4, 5.2, 3.6, 'pasta')
    p.pixels([(4, 9), (6, 10), (10, 10), (12, 9), (7, 11), (9, 11)], 'pasta', 0.0)
    p.toon(8.4, 6.8, 3.0, 2.2, 'sauce')
    p.pixels([(9, 4), (10, 4), (10, 5)], 'leaf', 1.0)
    return p.render()


def drumstick():
    p = Pic()
    p.rows([
        "ww.......",
        "www......",
        ".www.....",
        "..www.ww.",
        "...wwwwww",
        "....wwww.",
        "....www..",
        ".....ww..",
    ], {'w': ('bone', 0.5)}, ox=7, oy=7)
    p.pixels([(12, 11), (13, 11), (12, 12)], 'bone', 0.0)
    p.toon(6.4, 6.4, 5.6, 5.0, 'cooked', ang=0.6)
    return p.render(inner=False)


imgs = {'tomato': tomato(), 'cabbage': cabbage(), 'onion': onion(), 'garlic': garlic(), 'iron_knife': knife(),
        'beef_stew': stew(), 'spaghetti_bolognese': bolognese(), 'cooked_chicken_drumstick': drumstick()}
for k, im in imgs.items():
    im.save(f'{OUT}/{k}.png')
sheet(list(imgs.values()), f'{OUT}/_sheet.png')
