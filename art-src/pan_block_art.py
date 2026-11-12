import sys, math, random
from PIL import Image
out=sys.argv[1]
IRON=[(30,28,34),(46,44,52),(62,60,70),(84,82,94),(112,110,122)]
rnd=random.Random(2)
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        d=math.hypot(x-7.5,y-7.5)
        c=IRON[2]
        if 2.5<d<3.3 or 5.0<d<5.6: c=IRON[1]          # faint rings in the seasoned iron
        if rnd.random()<0.06: c=IRON[3]
        if x in (0,15) or y in (0,15): c=IRON[3]
        px[x,y]=c+(255,)
img.save(f"{out}/frying_pan.png")
