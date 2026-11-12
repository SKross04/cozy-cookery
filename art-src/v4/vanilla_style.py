"""Cozy Cookery art v4: drawn with the same technique as the current vanilla items (apple, carrot, potato).
- Hand-pixelled 16x16 grids, ~8-10 colours per item.
- Saturated base, small bright highlight cluster top-left, darker shade + darkest outline bottom-right.
- Outline uses the object's own dark colours: a warmer dark on the top-left, the darkest on the bottom-right.
- Light pixel texture (alternating two neighbouring shades) inside big areas, like vanilla.
"""
import sys
from PIL import Image


def hx(s):
    s = s.lstrip('#')
    return tuple(int(s[i:i + 2], 16) for i in (0, 2, 4)) + (255,)


def draw(rows, pal):
    assert len(rows) == 16, len(rows)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    for y, row in enumerate(rows):
        assert len(row) == 16, (y, row, len(row))
        for x, ch in enumerate(row):
            if ch != '.':
                p[x, y] = hx(pal[ch])
    return img


GREENS = {'a': '#33be30', 'b': '#1f9a1c', 'c': '#036703', 'g': '#005000'}   # vanilla carrot leaves

tomato = draw([
    "................",
    "........c.......",
    ".......bc.......",
    "....cbbab.bbc...",
    "...cAcabaabcAc..",
    "..ADEcbcgbcEDA..",
    ".ADEHIcGgGcHEDA.",
    ".ADHIFHGHGHGHEDO",
    ".AEIFIHHGHGGHEDO",
    ".AEHIHGHGGGHGEDO",
    ".AEGHGGEGGEGGEDO",
    ".ADEGGEGEEGEEDDO",
    "..ODEEEGEEEEDDO.",
    "...ODDEEEEDDDO..",
    "....OODDDDDOO...",
    "......OOOOO.....",
], {**GREENS, 'O': '#54090e', 'A': '#7a1a0e', 'D': '#9c1a17', 'E': '#c8261a', 'G': '#dd3a22',
    'H': '#ff4a2e', 'I': '#ff7a5e', 'F': '#ffa890'})

onion = draw([
    "................",
    "..........A.....",
    ".........AD.....",
    "........AD......",
    "......AADAA.....",
    "....AADGEDDAA...",
    "...ADGHIGEGEDA..",
    "..ADGIFIHGEGEDO.",
    "..AEHIIHGEGEEDO.",
    ".ADEHHGHEGEGEDDO",
    ".ADEGHGEGEEGEDDO",
    "..ADEGEEGEEEDDO.",
    "..ODDEEEEEEDDOO.",
    "...OODDEEDDOO...",
    ".....ORrRrO.....",
    "......r..r......",
], {'O': '#4a1e06', 'A': '#6e3008', 'D': '#9a4a12', 'E': '#c06a1e', 'G': '#d8842a', 'H': '#eca040',
    'I': '#f8c060', 'F': '#ffe0a0', 'r': '#c8a878', 'R': '#8a6a48'})

garlic = draw([
    "................",
    "........A.......",
    ".......AE.......",
    ".......AGA......",
    "......AGHEA.....",
    "....AAEGHGEAA...",
    "...ADEHFHGEGDA..",
    "..ADGHFHGEDEEDO.",
    "..AEHHGEGEDEEDO.",
    ".ADEHGEDGEDGEDDO",
    ".ADEGEDEGEDEEDDO",
    ".ADEGEDpGEDEpDO.",
    "..ODEEDPEEDPEDO.",
    "...OODDEEDDDOO..",
    ".....ODpDpDO....",
    "......O.O.O.....",
], {'O': '#4a2a3a', 'A': '#6a4a5a', 'D': '#a88a98', 'E': '#d8c8d0', 'G': '#ecdfe4', 'H': '#f8f2f4',
    'F': '#ffffff', 'P': '#a05a8a', 'p': '#c888b0'})

cabbage = draw([
    "................",
    "................",
    "......AAAAA.....",
    "....AAGHHGEAA...",
    "...AGHIIHGGEDA..",
    "..AGHIFIHGGEEDA.",
    "..AGHIIHGFGEDDA.",
    ".AAAAGHHFGEAAAAO",
    ".ADEDAGGFEEADEDO",
    ".ADGEDAGFEADEEDO",
    ".AEGGEDAFADEEDDO",
    ".ADEGEEDFDEEDDDO",
    "..ADEEEDFDEEDDO.",
    "..ODDEEDDDEDDOO.",
    "...OODDDDDDDO...",
    ".....OOOOOOO....",
], {'O': '#0e3a0c', 'A': '#1a5a14', 'D': '#2a7a1e', 'E': '#3e9a2a', 'G': '#5cb83a', 'H': '#84d052',
    'I': '#b0e47a', 'F': '#d8f4a8'})

knife = draw([
    ".............AAA",
    "............ABBC",
    "...........ABDBC",
    "..........ABDEBC",
    ".........ABDEEC.",
    "........ABDEEC..",
    ".......ABDEEC...",
    "......ABDEEC....",
    ".....AGEDEC.....",
    "....AGGFEC......",
    "...HIAFAC.......",
    "..HJKCAC........",
    ".HJKC...........",
    "HJKC............",
    "JKC.............",
    "CC..............",
], {'A': '#444444', 'B': '#ffffff', 'C': '#181818', 'D': '#bebebe', 'E': '#d8d8d8', 'F': '#6b6b6b',
    'G': '#969696', 'H': '#493615', 'I': '#684e1e', 'J': '#896727', 'K': '#281e0b'})

# Soup overlay for the vanilla bowl (minecraft:item/bowl stays layer0) - same area vanilla's stews use.
beef_stew = draw([
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    ".....BCKCBb.....",
    "...CDDKkDCPCB...",
    "....BCCDDCBp....",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
    "................",
], {'B': '#6e2a10', 'C': '#8a3a16', 'D': '#a44e20', 'K': '#ff8e09', 'k': '#d36a0d', 'P': '#e9ba62',
    'p': '#c8973a', 'b': '#33be30'})

drumstick = draw([
    "................",
    "....AAAA........",
    "...ABCDBAA......",
    "..AECBEEBEA.....",
    "..ADBEBCFFDG....",
    ".ADBDCFFFCBG....",
    ".AABCFFFCCDBG...",
    ".ADCECFFCCDDG...",
    ".ADDACEAADDBG...",
    "..ADBAACFCBG....",
    "...GAEAFCEGWW...",
    "....GGAAGWWwW...",
    "......GG.WwWWW..",
    ".........WWVWW..",
    "..........WVV...",
    "................",
], {'A': '#8b4420', 'B': '#cd7d4a', 'C': '#de9d7b', 'D': '#d28e62', 'E': '#ac5d31', 'F': '#e6bea4',
    'G': '#5a2c10', 'W': '#e8e2d4', 'w': '#ffffff', 'V': '#a8a090'})

SPRITES = {'tomato': tomato, 'onion': onion, 'garlic': garlic, 'cabbage': cabbage, 'iron_knife': knife,
           'beef_stew': beef_stew, 'cooked_chicken_drumstick': drumstick}

if __name__ == '__main__':
    out = sys.argv[1]
    for n, im in SPRITES.items():
        im.save(f'{out}/{n}.png')
