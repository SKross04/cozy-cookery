import sys, random
from PIL import Image
out=sys.argv[1]
CAB=[(48,96,40),(86,148,62),(128,186,92),(178,214,136)]
rnd=random.Random(3)
# head: light green with leaf folds and pale veins
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        c=CAB[2] if (x+y//2)%5 else CAB[1]
        if x in (7,8): c=CAB[3]
        if (x-y)%6==0: c=CAB[3]
        if rnd.random()<0.08: c=CAB[1]
        px[x,y]=c+(255,)
img.save(f"{out}/block/cabbage_head.png")
# outer leaf: darker green with a pale middle vein and side veins
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        c=CAB[1] if rnd.random()<0.8 else CAB[0]
        if x in (7,8): c=CAB[3]
        if (x+y)%5==0 and x<7 or (x-y)%5==0 and x>8: c=CAB[2]
        px[x,y]=c+(255,)
img.save(f"{out}/block/cabbage_leaf.png")
