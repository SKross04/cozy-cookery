import sys, math, random
from PIL import Image
out=sys.argv[1]
IRON=[(34,32,38),(52,50,58),(72,70,80),(98,96,108),(128,126,138)]
COPPER=[(110,56,32),(164,88,50),(204,124,72),(234,164,108)]
SOOT=[(26,22,24),(40,34,36),(56,48,50)]
rnd=random.Random(9)
def save(img,name): img.save(f"{out}/{name}.png")
# cast iron top plate with a copper burner ring
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        d=math.hypot(x-7.5,y-7.5)
        c=IRON[2]
        if 4.6<=d<=5.6: c=COPPER[2] if (x+y)%3 else COPPER[3]
        elif d<2.2: c=IRON[1]
        elif 2.2<=d<3.0: c=IRON[3]
        if x in (0,15) or y in (0,15): c=IRON[1]
        if (x,y) in ((1,1),(14,1),(1,14),(14,14)): c=IRON[4]            # bolts
        if rnd.random()<0.05 and c==IRON[2]: c=IRON[1]
        px[x,y]=c+(255,)
save(img,"stove_top")
# iron for the door frame
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        c=IRON[3] if y%8==0 else IRON[2]
        if x%5==2 and y%8==4: c=COPPER[3]                                # copper rivets
        if y%8==7: c=IRON[1]
        px[x,y]=c+(255,)
save(img,"stove_iron")
# sooty firebox bricks (unlit)
def soot_bricks(px, oy=0, glow=None, f=0):
    for y in range(16):
        for x in range(16):
            row=y//4; off=4 if row%2 else 0
            mortar = (y%4==3) or ((x+off)%8==7)
            c = SOOT[0] if mortar else SOOT[2 if (x*7+y*3)%5==0 else 1]
            if glow and mortar:
                t=math.sin(x*0.9+y*0.5+f*0.8)
                c = glow[2] if t>0.6 else glow[1] if t>-0.2 else glow[0]
            px[x,y+oy]=c+(255,)
img=Image.new("RGBA",(16,16)); soot_bricks(img.load()); save(img,"stove_firebox")
# lit firebox: glowing mortar cracks and ember bed, animated
GLOW=[(150,40,20),(230,110,30),(255,200,90)]
img=Image.new("RGBA",(16,128)); px=img.load()
for f in range(8):
    soot_bricks(px, 16*f, GLOW, f)
    for y in range(11,16):                     # ember bed at the bottom
        for x in range(16):
            t=math.sin(x*1.3+f*0.9)+math.sin(y*2.1-f*0.7)
            c = GLOW[2] if t>1.0 else GLOW[1] if t>-0.3 else GLOW[0]
            px[x,y+16*f]=c+(255,)
save(img,"stove_firebox_lit")
open(f"{out}/stove_firebox_lit.png.mcmeta","w").write('{\n  "animation": {\n    "frametime": 3,\n    "interpolate": true\n  }\n}\n')
