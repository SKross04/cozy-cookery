import sys, math, random
from PIL import Image
out=sys.argv[1]
IRON=[(34,32,38),(52,50,58),(72,70,80),(98,96,108),(128,126,138)]
COPPER=[(110,56,32),(164,88,50),(204,124,72),(234,164,108)]
SOOT=[(40,34,36),(66,58,58),(88,78,76)]
rnd=random.Random(9)
def save(img,name): img.save(f"{out}/{name}.png")
# cast iron top plate with a copper burner ring
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        d=math.hypot(x-7.5,y-7.5); c=IRON[2]
        if 4.6<=d<=5.6: c=COPPER[2] if (x+y)%3 else COPPER[3]
        elif d<2.2: c=IRON[1]
        elif 2.2<=d<3.0: c=IRON[3]
        if x in (0,15) or y in (0,15): c=IRON[1]
        if (x,y) in ((1,1),(14,1),(1,14),(14,14)): c=IRON[4]
        if rnd.random()<0.05 and c==IRON[2]: c=IRON[1]
        px[x,y]=c+(255,)
save(img,"stove_top")
# iron door frame with copper rivets
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        c=IRON[3] if y%8==0 else IRON[2]
        if x%5==2 and y%8==4: c=COPPER[3]
        if y%8==7: c=IRON[1]
        px[x,y]=c+(255,)
save(img,"stove_iron")
# sooty firebox walls: always the same, lit or not
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        row=y//4; off=4 if row%2 else 0
        mortar=(y%4==3) or ((x+off)%8==7)
        px[x,y]=(SOOT[0] if mortar else SOOT[2 if (x*7+y*3)%5==0 else 1])+(255,)
save(img,"stove_soot")
# cold ash bed (unlit)
img=Image.new("RGBA",(16,16)); px=img.load()
ASH=[(58,54,54),(88,84,82),(122,118,114)]
for y in range(16):
    for x in range(16):
        r=rnd.random(); px[x,y]=(ASH[2] if r<0.2 else ASH[0] if r<0.45 else ASH[1])+(255,)
save(img,"stove_ash")
# glowing ember bed (lit, animated)
EMB=[(120,30,16),(200,70,24),(246,140,40),(255,214,120)]
img=Image.new("RGBA",(16,128)); px=img.load()
seeds=[(rnd.random()*6.28, rnd.random()*6.28) for _ in range(256)]
for f in range(8):
    for y in range(16):
        for x in range(16):
            a,b=seeds[y*16+x]
            t=math.sin(a+f*0.785)+0.6*math.sin(b+f*1.57)
            c=EMB[3] if t>1.2 else EMB[2] if t>0.3 else EMB[1] if t>-0.6 else EMB[0]
            px[x,y+16*f]=c+(255,)
save(img,"stove_embers")
open(f"{out}/stove_embers.png.mcmeta","w").write('{\n  "animation": {\n    "frametime": 4,\n    "interpolate": true\n  }\n}\n')
# flames (lit, animated, transparent background)
FL=[(196,56,24),(240,120,32),(255,186,64),(255,238,170)]
img=Image.new("RGBA",(16,128),(0,0,0,0)); px=img.load()
for f in range(8):
    for x in range(16):
        edge=1.0-abs(x-7.5)/8.5
        h=(5+6*edge) + 2.2*math.sin(x*1.1+f*0.8) + 1.4*math.sin(x*2.3-f*1.4)
        h=max(0,min(15,h))
        for k in range(int(h)):
            y=15-k; rel=k/max(h,1)
            c=FL[3] if rel<0.3 and 4<=x<=11 else FL[2] if rel<0.55 else FL[1] if rel<0.8 else FL[0]
            px[x,y+16*f]=c+(255,)
save(img,"stove_flame")
open(f"{out}/stove_flame.png.mcmeta","w").write('{\n  "animation": {\n    "frametime": 2\n  }\n}\n')
