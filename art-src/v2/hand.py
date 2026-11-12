"""Hand-pixelled sprites (char grids). Each char -> (material, ramp level). '.' = empty."""
import sys, os
sys.path.insert(0, os.path.dirname(__file__))
from kit import R
from PIL import Image


def draw(rows, legend):
    assert len(rows) == 16, len(rows)
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    p = img.load()
    for y, row in enumerate(rows):
        assert len(row) == 16, (y, row, len(row))
        for x, ch in enumerate(row):
            if ch != '.':
                m, l = legend[ch]
                p[x, y] = R[m][l] + (255,)
    return img


def L(mat, letters):
    """letters[i] -> (mat, i), e.g. L('bowl', 'oqrstu')."""
    return {ch: (mat, i) for i, ch in enumerate(letters) if ch != ' '}


BOWL = L('bowl', 'oqrstu')

beef_stew = draw([
    "................",
    "................",
    "................",
    "......oono......",
    "....ooMnnMoo.o..",
    "..ooKCmMmmoogGo.",
    ".otKCcmMmmTSgGto",
    "otuCcSTTSSTSPPto",
    "otSSTTSnMSAPPPpo",
    "otSATSMMmASSppso",
    "otuuuuuuuuuuttro",
    "ostttttssssssrro",
    ".osssssssssrrrq.",
    "..oqssssssrrrqo.",
    "...ooqqqqqqqoo..",
    ".....oooooo.....",
], {**BOWL, 'A': ('stewred', 1), 'S': ('stewred', 2), 'T': ('stewred', 3),
    'c': ('carrot', 2), 'C': ('carrot', 3), 'K': ('carrot', 4),
    'm': ('cooked', 2), 'M': ('cooked', 3), 'n': ('cooked', 4),
    'p': ('potflesh', 2), 'P': ('potflesh', 4), 'g': ('leaf', 2), 'G': ('leaf', 4)})

PLATE = L('plate', 'abcdef')
bolognese = draw([
    "................",
    "................",
    "........gG......",
    "......oUSSGo....",
    "....oUUsSSSuo...",
    "...oYuSsVsSuYo..",
    "..oyYYuSSsuYYyo.",
    "..oYyYYuuuYYyYo.",
    "..oyYYyYYyYYyyo.",
    ".abdoyyYyyYyyoda",
    "abhdeoyyyyyoedha",
    "ahdeeeooooooedha",
    "ahccddeeeeedccha",
    ".ahhcccccccchha.",
    "..aahhhhhhhhaa..",
    "....aaaaaaaa....",
], {**PLATE, 'h': ('glaze', 3), 'y': ('pasta', 2), 'Y': ('pasta', 4), 'o': ('pasta', 0),
    's': ('sauce', 2), 'S': ('sauce', 3), 'u': ('sauce', 1), 'U': ('sauce', 0), 'V': ('sauce', 5),
    'g': ('leaf', 2), 'G': ('leaf', 4)})

cabbage = draw([
    "................",
    "......oooo......",
    "....oowlwlo.....",
    "...olwllwllo....",
    "..obllwlllbbo...",
    ".obbllwlllbbmo..",
    ".omOOObwbOOOmmo.",
    "omdOllbwbllOdmmo",
    "omdObllwllbOddmo",
    "odmOlwbwbwbOmddo",
    "odmObbwwwbmOmddo",
    "oddObmbwbmmOddmo",
    ".odOmmmwmmmOddo.",
    ".ooOOmmwmmOOdoo.",
    "...ooOOOOOOoo...",
    "................",
], {'o': ('cabbage', 0), 'O': ('cabbage', 0), 'd': ('cabbage', 1), 'm': ('cabbage', 2),
    'b': ('cabbage', 3), 'l': ('cabbage', 4), 'w': ('cabbage', 5)})

if __name__ == '__main__':
    out = sys.argv[1]
    for n, im in [('beef_stew', beef_stew), ('bolognese', bolognese), ('cabbage', cabbage)]:
        im.save(f'{out}/h_{n}.png')
    sc = 10
    c = Image.new('RGBA', (3 * 16 * sc, 16 * sc), (139, 139, 139, 255))
    for i, im in enumerate([beef_stew, bolognese, cabbage]):
        b = im.resize((16 * sc, 16 * sc), Image.NEAREST)
        c.paste(b, (i * 16 * sc, 0), b)
    c.save(f'{out}/h_zoom.png')
