"""Every remaining item texture in the v4 style (auto-shaded masks + hand details).
python items.py <textures/item folder>
"""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from autoshade import Piece, render, ellipse, rect, grid, band, minus, pal, hx

P = {
    'rawbeef': pal('#470a06', '#7b1713', '#ad1d17', '#e03e35', '#e2625a', '#ea8873'),
    'rawpork': pal('#512626', '#853e3e', '#a75353', '#ef7070', '#ff8c8c', '#ffc6c6'),
    'rawmutton': pal('#470a06', '#96211b', '#bc2922', '#d12e26', '#e2625a', '#e88a82'),
    'rawchicken': pal('#865245', '#b47b65', '#c19280', '#efbcac', '#f2c9bd', '#ffe7dc'),
    'cookedbeef': pal('#2a160d', '#3f2116', '#673728', '#8e523c', '#a86a4c', '#c88a68'),
    'cookedpork': pal('#5f4f27', '#81602b', '#997942', '#c5ad77', '#d3c088', '#e2d3ac'),
    'cookedmutton': pal('#2a160d', '#522f1f', '#7c402f', '#9d6147', '#b07a5e', '#c89878'),
    'cookedchicken': pal('#5a2c10', '#8b4420', '#ac5d31', '#cd7d4a', '#de9d7b', '#e6bea4'),
    'bacon': pal('#4a1408', '#7a2410', '#a83c1c', '#c85a2a', '#e88a52', '#f8b07a'),
    'carrot': pal('#752802', '#ac3900', '#d36a0d', '#ff8e09', '#ffa73f', '#ffc177'),
    'potflesh': pal('#7a5a1a', '#a8842a', '#d8b858', '#f0d880', '#f8e8a8', '#fff8d8'),
    'potskin': pal('#6d3701', '#9a5500', '#af8444', '#c8973a', '#d9aa51', '#e9ba62'),
    'fried': pal('#6d3701', '#9a5500', '#c8801a', '#e8a830', '#f8c850', '#ffe48a'),
    'beet': pal('#3e0e14', '#5b1d17', '#8f2d2f', '#a4272c', '#b6484c', '#c07279'),
    'tomato': pal('#54090e', '#7a1a0e', '#9c1a17', '#c8261a', '#dd3a22', '#ff7a5e'),
    'onionflesh': pal('#7a5a3a', '#a88a6a', '#d8c8a8', '#f4ecd8', '#fff8ec', '#ffffff'),
    'garlic': pal('#4a2a3a', '#6a4a5a', '#a88a98', '#d8c8d0', '#ecdfe4', '#ffffff'),
    'cabbage': pal('#0e3a0c', '#1a5a14', '#2a7a1e', '#5cb83a', '#84d052', '#b0e47a'),
    'mushroom': pal('#4a2a1a', '#6e4a32', '#a88468', '#d8bca0', '#ecd8c4', '#fff0e0'),
    'friedmush': pal('#3a1e0e', '#5e3418', '#86502a', '#a86a38', '#c48a52', '#dca870'),
    'appleflesh': pal('#7a5a2a', '#b89a5a', '#e0c890', '#f8e8b8', '#fff4d8', '#ffffff'),
    'pumpkin': pal('#5a2a04', '#a45413', '#c86a10', '#e88a1e', '#f8b040', '#ffd080'),
    'crust': pal('#3f2e0e', '#654b17', '#8c661e', '#a27924', '#bc8927', '#d8a840'),
    'crumb': pal('#8a6a2a', '#c0a060', '#e8d098', '#f4e4b8', '#faf0d4', '#ffffff'),
    'toast': pal('#3e1e06', '#6a3a0e', '#a8641c', '#d08a30', '#e8a848', '#f8c870'),
    'dough': pal('#6a5a3a', '#a8946a', '#d8c8a0', '#f0e6c8', '#faf4e2', '#ffffff'),
    'pasta': pal('#7a4a0a', '#b07818', '#d8a830', '#f0cc58', '#f8e088', '#fff4c0'),
    'pancake': pal('#6a3a0a', '#a0601a', '#c88430', '#e8a848', '#f4c470', '#fde0a0'),
    'egg': pal('#7a5a0a', '#c89a12', '#e8b820', '#fad44a', '#ffe888', '#fff8d0'),
    'eggwhite': pal('#8a8478', '#c8c2b4', '#e8e4da', '#f8f6f0', '#ffffff', '#ffffff'),
    'char': pal('#0a0808', '#1a1414', '#2a2220', '#3a302c', '#4e4440', '#6a5e58'),
    'sack': pal('#5a4a2a', '#8a7448', '#b8a070', '#d8c498', '#ece0c0', '#fbf6e8'),
    'wood': pal('#281e0b', '#493615', '#684e1e', '#896727', '#a88238', '#c49a4a'),
    'copper': pal('#3e1a0c', '#6e3016', '#a04a24', '#c06636', '#e08a4e', '#f8b07a'),
    'clay': pal('#373944', '#5e6c8d', '#757d90', '#9499a4', '#acaebd', '#c8cad8'),
    'plate': pal('#4a5060', '#8a9aac', '#b8c0cc', '#e2e6ea', '#f4f6f8', '#ffffff'),
    'bone': pal('#5a4a44', '#a8a090', '#d8d0c0', '#ece6da', '#f8f4ec', '#ffffff'),
}


def item(*pieces):
    return render(list(pieces))


# ---------- shapes ----------
def strips(mat, fat=None, marks=None):
    ps = []
    for k, (p0, a0, a1) in enumerate([(9, -6, 4), (13, -5, 6), (17, -3, 6)]):
        pc = Piece(band(p0, p0 + 2, a0, a1), P[mat])
        if fat:
            pc.at([(x, y) for (x, y) in pc.mask if x + y == p0 + 1 and (x - y) % 3 != 0], fat)
        if marks:
            pc.at([(x, y) for (x, y) in pc.mask if (x - y) % 4 == 0 and x + y > p0], marks)
        ps.append(pc)
    return item(*ps)


def cubes(mat, n=4, fat=None, size=5):
    spots = [(2, 3), (8, 2), (3, 9), (9, 8)] if size == 5 else [(2, 3), (7, 2), (11, 4), (3, 8), (8, 7), (5, 12), (10, 11)]
    ps = []
    for (x, y) in spots[:n if size == 5 else 7]:
        pc = Piece(rect(x, y, x + size - 1, y + size - 1), P[mat], shine=False, tex=False)
        pc.at([(x + i, y) for i in range(size)], P[mat]['l'])
        pc.at([(x, y)], P[mat]['h'])
        if fat and size == 5:
            pc.at([(x + 1, y + 2), (x + 2, y + 3)], fat)
        ps.append(pc)
    return item(*ps)


def heap(mat, specks=(), cx=8, cy=10, rx=6.2, ry=4.4):
    mask = ellipse(cx, cy, rx, ry) + ellipse(cx - 2, cy - 3, 2.6, 2.2) + ellipse(cx + 2, cy - 2.6, 2.8, 2.2)
    pc = Piece(mask, P[mat])
    pts = sorted(set(mask))
    for i, c in enumerate(specks):
        pc.at([q for j, q in enumerate(pts) if (j * 7 + i * 3) % 11 == 0], c)
    pc.at([q for j, q in enumerate(pts) if (j * 5) % 9 == 0], P[mat]['s'])
    return item(pc)


def balls(mat, specks=None):
    ps = []
    for (cx, cy) in [(8, 6), (5, 10.5), (11, 10.5)]:
        pc = Piece(ellipse(cx, cy, 3.4, 3.2), P[mat])
        if specks:
            pc.at([(int(cx), int(cy) + 1), (int(cx) - 1, int(cy) - 1)], specks)
        ps.append(pc)
    return item(*ps)


def coins(mat, rim=None, core=None, ring=None, hole=False, n=3, rx=3.6, ry=3.2):
    ps = []
    for (cx, cy) in [(5.5, 6), (10.5, 7), (7.5, 11)][:n]:
        mask = ellipse(cx, cy, rx, ry)
        if hole:
            mask = minus(mask, ellipse(cx, cy, rx * 0.42, ry * 0.42))
        pc = Piece(mask, P[mat], shine=not hole)
        if rim:
            edge = [q for q in pc.mask if any((q[0] + dx, q[1] + dy) not in pc.mask for dx, dy in ((1, 0), (0, 1)))]
            pc.at(edge, rim)
        if ring:
            pc.at([q for q in ellipse(cx, cy, rx * 0.62, ry * 0.62) if q not in ellipse(cx, cy, rx * 0.35, ry * 0.35)], ring)
        if core:
            pc.at(ellipse(cx, cy, 1.1, 1.0), core)
        ps.append(pc)
    return item(*ps)


def wedges(mat, skin, n=3):
    ps = []
    for (cx, cy) in [(6, 5), (10.5, 7.5), (6.5, 10.5)][:n]:
        mask = [q for q in ellipse(cx, cy, 4.4, 3.4) if q[1] + 0.5 >= cy - 0.6]
        pc = Piece(mask, P[mat])
        pc.at([q for q in mask if (q[0], q[1] + 1) not in mask or (q[0] + 1, q[1] + 1) not in mask], skin)
        pc.at([(int(cx), int(cy))], '#5a3a1a')
        ps.append(pc)
    return item(*ps)


def nest(mat, sauce=None):
    pc = Piece(ellipse(8, 9.5, 6.4, 4.8), P[mat], tex=False)
    pc.at([q for q in pc.mask if (q[0] + q[1]) % 3 == 0], P[mat]['s'])
    pc.at([q for q in pc.mask if (q[0] - q[1]) % 4 == 0 and (q[0] + q[1]) % 3], P[mat]['l'])
    ps = [pc]
    if sauce:
        ps.append(Piece(ellipse(8.4, 8.2, 3, 2), P[sauce]))
    return item(*ps)


def disc(mat, cx=8, cy=9.5, rx=6.6, ry=4.6, top=None):
    pc = Piece(ellipse(cx, cy, rx, ry), P[mat])
    ps = [pc]
    if top:
        ps.append(top)
    return item(*ps)


def dough_ball(mat, specks=None):
    pc = Piece(ellipse(8, 10, 6.4, 4.6) + ellipse(8, 8, 4.6, 3.6), P[mat])
    if specks:
        pc.at([(5, 9), (9, 7), (11, 10), (7, 11), (6, 7), (10, 12)], specks)
    return item(pc)


def slice_of_bread(crust, crumb, specks=None):
    shape = grid([
        "...######...",
        "..########..",
        ".##########.",
        ".##########.",
        "..########..",
        "..########..",
        "..########..",
        "..########..",
        "..########..",
        "..########..",
        "..########..",
    ], ox=2, oy=3)
    inner = grid([
        "....####....",
        "...######...",
        "..########..",
        "..########..",
        "...######...",
        "...######...",
        "...######...",
        "...######...",
        "...######...",
    ], ox=2, oy=4)
    a = Piece(shape, P[crust])
    b = Piece(inner, P[crumb], outline=False)
    if specks:
        b.at([(6, 6), (9, 8), (7, 10), (10, 11), (6, 12)], specks)
    return item(a, b)


def pie(filling, crust='pancake', tin='copper', raw=False):
    t = Piece(ellipse(8, 9, 7.4, 5.4), P[tin], shine=False)
    c = Piece(ellipse(8, 8.6, 6.2, 4.4), P['dough' if raw else crust], outline=False)
    f = Piece(ellipse(8, 8.6, 5.0, 3.4), P[filling], outline=False, shine=False)
    lattice = [q for q in f.mask if q[0] % 3 == 0 or q[1] % 3 == 0]
    f.at(lattice, P['dough' if raw else crust]['l'])
    return item(t, c, f)


def sack(mat='sack', powder='#ffffff'):
    shape = grid([
        ".....####.....",
        "......##......",
        "....######....",
        "...########...",
        "..##########..",
        ".############.",
        ".############.",
        ".############.",
        ".############.",
        "..##########..",
        "...########...",
    ], ox=1, oy=3)
    pc = Piece(shape, P[mat])
    pc.at([(7, 4), (8, 4)], '#9a7a42')
    pc.at([(6, 9), (7, 9), (8, 9), (9, 9), (6, 10), (9, 10), (6, 11), (7, 11), (8, 11), (9, 11)], '#c8b088')
    pc.at([(6, 3), (7, 3), (8, 3), (9, 3)], powder)
    return item(pc)


SPOON = [
    "............###.",
    "...........#####",
    "...........#####",
    "..........######",
    "..........#####.",
    ".........####...",
    "........##......",
    ".......##.......",
    "......##........",
    ".....##.........",
    "....##..........",
    "...##...........",
    "..##............",
    ".##.............",
    "##..............",
    "#...............",
]
SPATULA = [
    "..........#####.",
    ".........######.",
    "........#######.",
    ".......#######..",
    "........#####...",
    "........####....",
    ".......##.......",
    "......##........",
    ".....##.........",
    "....##..........",
    "...##...........",
    "..##............",
    ".##.............",
    "##..............",
    "#...............",
    "................",
]


def tool(rows, slots=()):
    pc = Piece(grid(rows), P['wood'], tex=False)
    pc.at(slots, P['wood']['o'])
    return item(pc)


def plate(mat='plate'):
    rim = Piece(ellipse(8, 8.6, 7.4, 6.2), P[mat], shine=False)
    well = Piece(ellipse(8, 8.2, 4.8, 3.8), P[mat], outline=False, flat='s')
    return item(rim, well)


def trellis():
    m = set()
    for x in (3, 8, 12):
        m |= set(rect(x, 1, x, 14))
    for y in (2, 7, 12):
        m |= set(rect(2, y, 13, y))
    for i in range(11):
        m.add((3 + i, 2 + i))
    return item(Piece(m, P['wood'], tex=False, shine=False))


def scraps():
    ps = [Piece(ellipse(cx, cy, r, r * 0.8), P['char']) for (cx, cy, r) in [(5, 6, 3), (10.5, 8, 3.2), (6, 11.5, 2.6)]]
    ps[1].at([(10, 8)], '#ff6a1a')
    ps[0].at([(5, 6)], '#c83a0a')
    return item(*ps)


def wing(mat):
    shape = grid([
        "........###.",
        "......######",
        "....########",
        "..#########.",
        ".#########..",
        "##########..",
        "#########...",
        ".#######....",
        "..####......",
    ], ox=2, oy=4)
    return item(Piece(shape, P[mat]))


def drumstick(mat):
    meat = Piece(ellipse(6.4, 6.4, 5.4, 4.8, 0.7) + ellipse(9.4, 9.4, 2.4, 2.4), P[mat])
    bone = Piece(grid([
        "xx....",
        "xxx...",
        ".xxx.x",
        "..xxxx",
        "...xxx",
        "..xxx.",
    ], 'x', ox=10, oy=9), P['bone'], tex=False)
    return item(bone, meat)


def fried_egg():
    white = Piece(ellipse(8, 9, 6.6, 5) + ellipse(5, 7, 3, 2.6) + ellipse(11, 11, 3, 2.4), P['eggwhite'], shine=False)
    yolk = Piece(ellipse(8, 8.4, 2.6, 2.3), P['egg'])
    return item(white, yolk)


def omelette(mix=False):
    mask = [q for q in ellipse(8, 10, 7, 4.6) if q[1] >= 8] + ellipse(8, 8.4, 6.6, 2.2)
    pc = Piece(mask, P['egg'])
    pc.at([(5, 10), (9, 12), (11, 9), (7, 9)], '#c88a12')
    pc.at([(6, 11), (10, 10)], '#4c9a22')
    return item(pc)


def pumpkin_slice():
    mask = grid([
        "......##.....",
        ".....####....",
        "....######...",
        "...########..",
        "..##########.",
        ".############",
        "#############",
    ], ox=1, oy=5)
    pc = Piece(mask, P['pumpkin'])
    pc.at([q for q in mask if q[1] >= 10], P['pumpkin']['s'])
    pc.at([q for q in mask if q[1] == 9 and q[0] % 2], '#fff0b0')
    return item(pc)


def onion_strings(mat='friedmush'):
    ps = []
    for (cx, cy, a) in [(6, 7, 0.5), (10, 8, -0.4), (7, 11, 0.1), (11, 12, 0.6)]:
        ring = minus(ellipse(cx, cy, 3.2, 2.2, a), ellipse(cx, cy, 1.6, 0.9, a))
        ps.append(Piece(ring, P[mat], shine=False))
    return item(*ps)


ITEMS = {
    # chopped produce
    'tomato_slices': lambda: coins('tomato', ring='#ffb090', core='#f8d070'),
    'diced_tomato': lambda: cubes('tomato', size=3),
    'onion_rings': lambda: coins('onionflesh', hole=True, ring='#e8d8b8'),
    'diced_onion': lambda: cubes('onionflesh', size=3),
    'minced_garlic': lambda: heap('garlic', specks=['#ffffff']),
    'shredded_cabbage': lambda: heap('cabbage', specks=['#b0e47a', '#d8f4a8']),
    'carrot_slices': lambda: coins('carrot', ring='#ffc177', core='#ffa73f'),
    'carrot_cubes': lambda: cubes('carrot'),
    'potato_slices': lambda: coins('potflesh', rim='#af8444'),
    'potato_cubes': lambda: cubes('potflesh'),
    'beetroot_cubes': lambda: cubes('beet'),
    'sliced_mushrooms': lambda: coins('mushroom', rim='#8a5a3a'),
    'apple_slices': lambda: wedges('appleflesh', '#dd1725'),
    'pumpkin_slice': pumpkin_slice,
    # raw meat
    'raw_beef_strips': lambda: strips('rawbeef', fat='#f4d0c4'),
    'raw_beef_cubes': lambda: cubes('rawbeef', fat='#f4d0c4'),
    'ground_beef': lambda: heap('rawbeef', specks=['#f4d0c4']),
    'raw_pork_strips': lambda: strips('rawpork', fat='#fff0ee'),
    'raw_pork_cubes': lambda: cubes('rawpork', fat='#fff0ee'),
    'ground_pork': lambda: heap('rawpork', specks=['#fff0ee']),
    'raw_mutton_strips': lambda: strips('rawmutton', fat='#f0dcd0'),
    'raw_mutton_cubes': lambda: cubes('rawmutton', fat='#f0dcd0'),
    'ground_mutton': lambda: heap('rawmutton', specks=['#f0dcd0']),
    'raw_chicken_drumstick': lambda: drumstick('rawchicken'),
    'raw_chicken_wing': lambda: wing('rawchicken'),
    'raw_meatballs': lambda: balls('rawbeef', specks='#f4d0c4'),
    # cooked meat
    'seared_beef_strips': lambda: strips('cookedbeef', marks='#2a160d'),
    'seared_beef_cubes': lambda: cubes('cookedbeef'),
    'browned_ground_beef': lambda: heap('cookedbeef'),
    'bacon': lambda: strips('bacon', fat='#f6d8a8'),
    'fried_pork_cubes': lambda: cubes('cookedpork'),
    'browned_ground_pork': lambda: heap('cookedpork'),
    'seared_mutton_strips': lambda: strips('cookedmutton', marks='#2a160d'),
    'seared_mutton_cubes': lambda: cubes('cookedmutton'),
    'cooked_chicken_wing': lambda: wing('cookedchicken'),
    'meatballs': lambda: balls('cookedbeef'),
    # fried sides
    'potato_chips': lambda: coins('fried', ring='#ffe48a'),
    'fried_potato_cubes': lambda: cubes('fried'),
    'fried_mushrooms': lambda: coins('friedmush', rim='#3a1e0e'),
    'fried_onions': lambda: onion_strings(),
    'fried_egg': fried_egg,
    'omelette': omelette,
    # flour, dough, baking
    'flour': lambda: sack(),
    'bread_dough': lambda: dough_ball('dough'),
    'bread_slice': lambda: slice_of_bread('crust', 'crumb'),
    'toast': lambda: slice_of_bread('toast', 'pancake'),
    'raw_garlic_bread': lambda: slice_of_bread('crust', 'crumb', specks='#4cb030'),
    'garlic_bread': lambda: slice_of_bread('toast', 'pancake', specks='#3c8a20'),
    'cookie_dough': lambda: dough_ball('dough', specks='#4b2817'),
    'raw_pumpkin_pie': lambda: pie('pumpkin', raw=True),
    'raw_apple_pie': lambda: pie('appleflesh', raw=True),
    'apple_pie': lambda: pie('appleflesh'),
    'pancake': lambda: disc('pancake'),
    'pasta_dough': lambda: dough_ball('pasta'),
    'raw_noodles': lambda: nest('dough'),
    'cooked_noodles': lambda: nest('pasta'),
    # tools, plates, misc
    'wooden_spoon': lambda: tool(SPOON),
    'wooden_spatula': lambda: tool(SPATULA, slots=[(11, 1), (10, 2), (12, 2), (11, 3)]),
    'plate': lambda: plate('plate'),
    'raw_plate': lambda: plate('clay'),
    'trellis': trellis,
    'charred_scraps': scraps,
}

if __name__ == '__main__':
    out = sys.argv[1]
    only = sys.argv[2:] or list(ITEMS)
    for n in only:
        ITEMS[n]().save(f'{out}/{n}.png')
    print(len(only), 'items')
