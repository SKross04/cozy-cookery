"""All bowl dishes (ours + vanilla stews) in the v4 style: round wooden bowl seen from above, food heaped inside.
python bowls.py <assets folder of the repo: src/main/resources/assets>
"""
import sys, os
from PIL import Image
from vanilla_style import hx

BOWL = [
    "................",
    ".....OOOOOO.....",
    "...OOrLLLLrOO...",
    "..OrL######LrO..",
    ".OLr########RLO.",
    "OLr##########RrO",
    "OLR##########RrO",
    "OrR##########RrO",
    "OrR##########RrO",
    ".OrR########RrO.",
    ".OLrR######RrO..",
    "..OLrRRRRRRRrO..",
    "..OlLrrRRRrrRO..",
    "...OOlLLLrrOO...",
    ".....OOOOOO.....",
    "................",
]
WOOD = {'O': '#3a2412', 'R': '#5e3e20', 'r': '#8a5e30', 'L': '#b07c40', 'l': '#cf9a58'}
INSIDE = [(x, y) for y, row in enumerate(BOWL) for x, ch in enumerate(row) if ch == '#']

# chunk slots for 3x2 pieces (top-left corner); every piece casts a dark pixel row under it
SLOTS = [(4, 4), (9, 4), (6, 6), (3, 8), (10, 7), (7, 9), (5, 3), (10, 3)]
SPECKS = [(8, 4), (12, 6), (4, 7), (9, 9), (6, 10), (11, 9)]


def mixc(a, b, t):
    a, b = hx(a), hx(b)
    return tuple(int(a[i] + (b[i] - a[i]) * t) for i in range(3)) + (255,)


def put_piece(p, sx, sy, light, dark, shadow):
    hi = mixc(light, '#ffffff', 0.45)
    pat = {(0, 0): hx(light), (1, 0): hi, (2, 0): hx(light), (0, 1): hx(dark), (1, 1): hx(light), (2, 1): hx(dark)}
    for (dx, dy), c in pat.items():
        if (sx + dx, sy + dy) in INSIDE:
            p[sx + dx, sy + dy] = c
    for dx in range(3):
        q = (sx + dx, sy + 2)
        if q in INSIDE:
            p[q] = hx(shadow)


def broth_shade(x, y):
    # light top-left, dark along the bottom-right inner edge, a little pixel texture in between
    edge = (x - 8) + (y - 7)
    if edge >= 6 or (x, y + 1) not in INSIDE or (x + 1, y) not in INSIDE:
        return 0
    if edge <= -5:
        return 2
    return 1 if (x + y) % 3 else (2 if edge < 0 else 0)


PLATE = [
    "................",
    ".....OOOOOO.....",
    "...OOBCCCCBOO...",
    "..OBC######JCBO.",
    ".OBC########JCO.",
    "OBC##########JBO",
    "OC###########JJO",
    "OC###########JJO",
    "OC###########JJO",
    "OBC#########JJBO",
    ".OBJ########JBO.",
    ".OIBJJ####JJBIO.",
    "..OIBJJJJJJBIO..",
    "...OOIIBBIIOO...",
    ".....OOOOOO.....",
    "................",
]
CHINA = {'O': '#4a5060', 'B': '#b8c0cc', 'C': '#ffffff', 'J': '#e2e6ea', 'I': '#8a9aac'}
VESSELS = {'bowl': (BOWL, WOOD), 'plate': (PLATE, CHINA)}


def dish(broth, pieces=(), specks=(), swirl=None, full=None, vessel='bowl'):
    global INSIDE
    tmpl, colours = VESSELS[vessel]
    INSIDE = [(x, y) for y, row in enumerate(tmpl) for x, ch in enumerate(row) if ch == '#']
    """broth: (dark, mid, light). pieces: list of (slot, (light, dark)). specks: list of colours.
    swirl: colour of a cream swirl. full: colour pattern function overriding broth (salads, pasta)."""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    for y, row in enumerate(tmpl):
        for x, ch in enumerate(row):
            if ch in colours:
                p[x, y] = hx(colours[ch])
    for (x, y) in INSIDE:
        p[x, y] = hx(full(x, y) if full else broth[broth_shade(x, y)])
    if swirl:
        for (x, y) in [(4, 7), (5, 6), (6, 6), (7, 7), (8, 8), (9, 8), (10, 7), (11, 6)]:
            p[x, y] = hx(swirl)
        for (x, y) in [(5, 7), (8, 9), (9, 9)]:
            p[x, y] = mixc(swirl, broth[0], 0.5)
    for slot, (light, dark) in pieces:
        sx, sy = SLOTS[slot]
        put_piece(p, sx, sy, light, dark, broth[0] if broth else '#1a1a1a')
    for i, c in enumerate(specks):
        x, y = SPECKS[i % len(SPECKS)]
        p[x, y] = hx(c)
    return img


# ingredient piece colours (light, dark)
CARROT = ('#ff9a1a', '#d36a0d')
BEEF = ('#9a4428', '#6e2c1c')
POTATO = ('#f4dc8a', '#c8a040')
CHICKEN = ('#f2d2b0', '#d0a07a')
MUTTON = ('#a85a3a', '#7a3624')
RABBIT = ('#c88a5a', '#9a5e38')
MUSHROOM = ('#9a6440', '#5e3a24')
CABBAGE = ('#9ee05a', '#5cae32')
ONION = ('#fff0c8', '#e8c890')
BREAD = ('#e8a848', '#a86420')
CHAR = ('#3a3030', '#1a1414')
NOODLE = ('#ffe08a', '#e8b84a')
TOMATO = ('#ff5a3a', '#c8201a')
HERB = '#4cb030'
HERB2 = '#8ccc3a'


def salad(base_cols, bits):
    def f(x, y):
        return base_cols[0] if (x + y) % 2 else base_cols[-1]
    return f


def pasta(sauce):
    def f(x, y):
        dx, dy = x - 8, y - 6.5
        if dx * dx / 10 + dy * dy / 4.5 <= 1:
            if dx * dx / 10 + dy * dy / 4.5 > 0.55 and (dx > 0 or dy > 0):
                return sauce[0]
            return '#ff8a6a' if (x, y) in ((7, 5), (6, 6)) else sauce[1]
        return NOODLE[0] if (x - y) % 3 else NOODLE[1]
    return f


LEAF = ('#9ee05a', '#4c9a22')
LEAF2 = ('#7cc83a', '#3c7a1c')
DISHES = {
    # ours
    'beef_stew': dict(broth=('#6a2410', '#8e3418', '#b04a20'),
                      pieces=[(0, CARROT), (1, BEEF), (2, POTATO), (3, BEEF), (4, CARROT)], specks=[HERB, HERB2]),
    'mutton_stew': dict(broth=('#4e2a14', '#6e3c1e', '#8e5228'),
                        pieces=[(0, POTATO), (1, MUTTON), (2, CARROT), (3, MUTTON), (4, POTATO)], specks=[HERB]),
    'chicken_soup': dict(broth=('#c8901e', '#e8b43a', '#f8d466'),
                         pieces=[(0, CARROT), (1, CHICKEN), (2, NOODLE), (3, CHICKEN), (4, CARROT)],
                         specks=[HERB, HERB2, HERB]),
    'vegetable_soup': dict(broth=('#b0401a', '#d0602a', '#e8803c'),
                           pieces=[(0, POTATO), (1, CABBAGE), (2, CARROT), (3, CABBAGE), (4, POTATO)],
                           specks=[HERB2]),
    'cabbage_soup': dict(broth=('#a0a03a', '#c0c050', '#d8d870'),
                         pieces=[(0, CABBAGE), (1, CARROT), (2, CABBAGE), (3, POTATO), (4, CABBAGE)]),
    'pumpkin_soup': dict(broth=('#d0600a', '#f08a1a', '#ffae3a'), swirl='#fff4dc', specks=['#3c8a20', '#3c8a20']),
    'tomato_soup': dict(broth=('#a8141a', '#d02a1e', '#f04a30'), swirl='#fff4dc', specks=[HERB, HERB2]),
    'onion_soup': dict(broth=('#8a4a10', '#b46a1a', '#d8902e'),
                       pieces=[(0, ONION), (2, BREAD), (4, ONION), (3, ONION)], specks=['#fff0c8']),
    'burnt_stew': dict(broth=('#1a1414', '#2a2220', '#3e3430'), pieces=[(0, CHAR), (2, CHAR), (4, CHAR)],
                       specks=['#5a4a40']),
    'coleslaw': dict(broth=('#c8c09a', '#f4f0d8', '#ffffff'), full=salad(['#f4f0d8', '#e0d8b4'], None),
                     pieces=[(0, LEAF), (1, ('#d070b0', '#9a3a80')), (2, ('#ff9a1a', '#d36a0d')), (3, ('#d070b0', '#9a3a80')),
                             (4, LEAF), (5, ('#ff9a1a', '#d36a0d'))]),
    'garden_salad': dict(broth=('#2a6a14', '#3c8a20', '#5cae32'), full=salad(['#3c8a20', '#2e7a18'], None),
                         pieces=[(6, LEAF), (7, LEAF2), (0, TOMATO), (1, LEAF), (2, ONION), (3, LEAF2), (4, TOMATO),
                                 (5, ('#ff9a1a', '#d36a0d'))]),
    'fruit_salad': dict(broth=('#c89040', '#f4d890', '#fff0c0'), full=salad(['#f4d890', '#e8c070'], None),
                        pieces=[(0, ('#ff3a2a', '#a8141a')), (1, ('#ffe24a', '#d8a810')), (2, ('#ff6a8a', '#c83a5a')),
                                (3, ('#8ad040', '#4c9a22')), (4, ('#ff3a2a', '#a8141a')), (5, ('#ffe24a', '#d8a810'))]),
    'spaghetti_pomodoro': dict(broth=('#a8141a',) * 3, full=pasta(('#a8141a', '#e83a2a')), specks=[HERB]),
    'spaghetti_bolognese': dict(broth=('#4a120a',) * 3, full=pasta(('#6a1a10', '#c8301e')), specks=[HERB]),
    'spaghetti_and_meatballs': dict(broth=('#a8141a',) * 3, full=pasta(('#a8141a', '#e83a2a')),
                                    pieces=[(2, ('#9a4428', '#5e2414')), (4, ('#9a4428', '#5e2414')), (3, ('#9a4428', '#5e2414'))]),
    # batters / mixes (no herbs)
    'cake_batter': dict(broth=('#e0c070', '#f4dc98', '#fff0c8'), swirl='#fff8e8'),
    'pancake_batter': dict(broth=('#e8d4a0', '#f6e8c4', '#fffbe8'), swirl='#ffffff'),
    'omelette_mix': dict(broth=('#e8a818', '#fad040', '#ffe888'), specks=['#4c9a22', '#4c9a22', '#ffffff']),
    # vanilla stews, redrawn to match
    'minecraft:mushroom_stew': dict(broth=('#b08060', '#d0a07c', '#e8c4a0'),
                                    pieces=[(0, MUSHROOM), (2, ('#e04a3a', '#a82a22')), (4, MUSHROOM), (3, MUSHROOM)]),
    'minecraft:beetroot_soup': dict(broth=('#6e0a24', '#9a1434', '#c42448'), swirl='#ffe0ec', specks=[HERB]),
    'minecraft:rabbit_stew': dict(broth=('#7a4418', '#9a5a22', '#b8742e'),
                                  pieces=[(0, CARROT), (1, RABBIT), (2, POTATO), (3, MUSHROOM), (4, RABBIT)],
                                  specks=[HERB]),
    'minecraft:suspicious_stew': dict(broth=('#b08060', '#d0a07c', '#e8c4a0'),
                                      pieces=[(0, MUSHROOM), (2, ('#e04a3a', '#a82a22')), (4, MUSHROOM)],
                                      specks=['#ffe24a', '#4a8aff', '#ffe24a']),
}

if __name__ == '__main__':
    A = sys.argv[1]
    for name, d in DISHES.items():
        img = dish(d.get('broth') or ('#000000',) * 3, d.get('pieces', ()), d.get('specks', ()), d.get('swirl'),
                   d.get('full'))
        ns, n = name.split(':') if ':' in name else ('cozycookery', name)
        os.makedirs(f'{A}/{ns}/textures/item', exist_ok=True)
        img.save(f'{A}/{ns}/textures/item/{n}.png')
    print(len(DISHES), 'bowls')
