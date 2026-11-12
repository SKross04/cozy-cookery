import sys, math, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
IRON=[(34,32,38),(52,50,58),(72,70,80),(98,96,108),(128,126,138)]
COPPER=[(110,56,32),(164,88,50),(204,124,72),(234,164,108)]
def tex(fn, painter, h=16):
    img=Image.new("RGBA",(16,h)); px=img.load()
    for y in range(h):
        for x in range(16): px[x,y]=painter(x,y)+(255,)
    img.save(f"{out}/{fn}.png")
rnd=random.Random(4)
def side(x,y):
    if y==7: return COPPER[3]
    if y==8: return COPPER[2] if x%4 else COPPER[1]       # rivets in the copper band
    if y==9: return IRON[1]
    if y>=14: return IRON[1]
    c=IRON[2]
    if x in (4,5): c=IRON[3]
    if x==4 and y in (10,11): c=IRON[4]
    if x in (12,13): c=IRON[1]
    if rnd.random()<0.06: c=IRON[1]
    return c
tex("cooking_pot_side", side)
tex("cooking_pot_inner", lambda x,y: IRON[1] if rnd.random()<0.8 else IRON[0])
tex("cooking_pot_top", lambda x,y: COPPER[3] if (x+y)%5 else COPPER[2])
tex("cooking_pot_bottom", lambda x,y: IRON[0] if (x in (2,13) or y in (2,13)) else IRON[1])
# animated water (8 frames)
W=[(176,176,176),(198,198,198),(220,220,220),(250,250,250)]  # greyscale, tinted in-game
img=Image.new("RGBA",(16,128)); px=img.load()
for f in range(8):
    for y in range(16):
        for x in range(16):
            v=math.sin(x*0.8+f*0.785)+math.sin(y*0.7-f*0.785)+0.6*math.sin((x+y)*0.45+f*0.39)
            c = W[3] if v>1.9 else W[2] if v>0.6 else W[1] if v>-0.9 else W[0]
            px[x,y+16*f]=c+(255,)
img.save(f"{out}/cooking_pot_water.png")
open(f"{out}/cooking_pot_water.png.mcmeta","w").write('{\n  "animation": {\n    "frametime": 3,\n    "interpolate": true\n  }\n}\n')
# animated soup (8 frames): warm broth with floating bits
S=[(168,168,168),(196,196,196),(220,220,220),(246,246,246)]  # greyscale, tinted in-game
bits=[(rnd.randrange(16),rnd.randrange(16),rnd.choice([(255,255,255),(140,140,140)])) for _ in range(9)]
img=Image.new("RGBA",(16,128)); px=img.load()
for f in range(8):
    for y in range(16):
        for x in range(16):
            v=math.sin(x*0.6+f*0.785)+math.sin(y*0.5-f*0.785)
            c = S[3] if v>1.7 else S[2] if v>0.4 else S[1] if v>-1.0 else S[0]
            px[x,y+16*f]=c+(255,)
    for bx,by,bc in bits:
        px[(bx+f//2)%16, (by+16*f)]=bc+(255,)
img.save(f"{out}/cooking_pot_soup.png")
open(f"{out}/cooking_pot_soup.png.mcmeta","w").write('{\n  "animation": {\n    "frametime": 4,\n    "interpolate": true\n  }\n}\n')
