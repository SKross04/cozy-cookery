"""Plated meals (item icon + block top/side textures) in the v4 style.
python plates.py <assets/cozycookery/textures folder>
"""
import sys, os, json
sys.path.insert(0, os.path.dirname(__file__))
import bowls
from bowls import dish, DISHES, hx
from PIL import Image

C = {  # component -> (light, dark) piece colours
    'seared_beef_strips': ('#a86a4c', '#673728'), 'seared_beef_cubes': ('#a86a4c', '#673728'),
    'seared_mutton_strips': ('#b07a5e', '#7c402f'), 'fried_pork_cubes': ('#d3c088', '#997942'),
    'potato_chips': ('#ffe48a', '#c8801a'), 'fried_potato_cubes': ('#f8c850', '#c8801a'),
    'fried_onions': ('#c48a52', '#86502a'), 'cooked_chicken_drumstick': ('#de9d7b', '#ac5d31'),
    'cooked_chicken_wing': ('#de9d7b', '#ac5d31'), 'cabbage_leaves': ('#84d052', '#2a7a1e'),
    'shredded_cabbage': ('#b0e47a', '#5cb83a'), 'carrot_slices': ('#ffa73f', '#d36a0d'),
    'diced_tomato': ('#ff5a3a', '#c8201a'), 'bacon': ('#e88a52', '#a83c1c'), 'omelette': ('#ffe888', '#e8b820'),
    'pancake': ('#f4c470', '#c88430'), 'minecraft:sweet_berries': ('#e0303a', '#8a0a1a'),
    'minecraft:cooked_cod': ('#f0e6d8', '#c8b498'), 'minecraft:cooked_salmon': ('#f8a080', '#d0604a'),
    'minecraft:baked_potato': ('#f0cd5a', '#9a5500'),
}
CHINA_INSIDE = ('#c8ccd4', '#e2e6ea', '#f4f6f8')


def block_textures(icon, T, name):
    top = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    top.paste(icon.crop((4, 3, 12, 11)), (4, 4))
    side = Image.new('RGBA', (16, 16))
    cols = [icon.getpixel((x, y)) for x in range(4, 12) for y in range(4, 10) if icon.getpixel((x, y))[3]]
    cols.sort(key=lambda c: sum(c[:3]))
    dark, mid = cols[len(cols) // 4], cols[len(cols) // 2]
    sp = side.load()
    for y in range(16):
        for x in range(16):
            sp[x, y] = mid if (y + x // 4) % 3 else dark
    top.save(f'{T}/block/{name}_top.png')
    side.save(f'{T}/block/{name}_side.png')


if __name__ == '__main__':
    T = sys.argv[1]
    spec = json.load(open(os.path.join(os.path.dirname(__file__), '..', 'meals.json')))
    n = 0
    for m in spec['component_meals']:
        comps = m['components']
        order = [comps[0], comps[1], comps[2], comps[3], comps[0], comps[1]]
        if m['name'] == 'pancake_stack':
            pieces = [(s, C['pancake']) for s in (6, 7, 0, 1, 2, 3, 4, 5)]
            specks = ['#e0303a', '#e0303a', '#8a0a1a', '#e0303a']
        else:
            cyc = [comps[i % 4] for i in (1, 2, 0, 3, 1, 2, 0, 3)]
            pieces = [(s, C[c]) for s, c in zip((6, 7, 0, 1, 2, 3, 4, 5), cyc)]
            specks = ['#4cb030'] if 'cabbage_leaves' not in comps else []
        lo, hi = C[comps[0]][1], C[comps[0]][0]
        base = (bowls.mixc(lo, '#000000', 0.25)[:3], hx(lo)[:3], hx(hi)[:3])
        base = tuple('#%02x%02x%02x' % c for c in base)
        icon = dish(base, pieces, specks, vessel='plate')
        icon.save(f'{T}/item/{m["name"]}.png')
        block_textures(icon, T, m['name'])
        n += 1
    # full breakfast (made before meals.json existed): fried egg, bacon, toast, mushrooms
    fb = dish(('#c8ccd4', '#e2e6ea', '#f4f6f8'),
              [(6, ('#e88a52', '#a83c1c')), (7, ('#e88a52', '#a83c1c')), (0, ('#f8c870', '#a8641c')),
               (4, ('#c48a52', '#86502a')), (3, ('#f8c870', '#a8641c'))],
              vessel='plate')
    for (x, y) in [(8, 6), (9, 6), (8, 7), (9, 7)]:
        fb.putpixel((x, y), hx('#fad44a'))
    for (x, y) in [(7, 5), (8, 5), (9, 5), (10, 5), (7, 6), (10, 6), (7, 7), (10, 7), (8, 8), (9, 8), (10, 8)]:
        fb.putpixel((x, y), hx('#ffffff'))
    fb.save(f'{T}/item/full_breakfast.png')
    block_textures(fb, T, 'full_breakfast')
    n += 1
    for b in spec['bowl_dishes']:
        base = b['name'].replace('plated_', '')
        d = DISHES.get(base) or DISHES['minecraft:' + base]
        icon = dish(d.get('broth') or ('#000000',) * 3, d.get('pieces', ()), d.get('specks', ()), d.get('swirl'),
                    d.get('full'), vessel='plate')
        icon.save(f'{T}/item/{b["name"]}.png')
        block_textures(icon, T, b['name'])
        n += 1
    print(n, 'plated meals')
