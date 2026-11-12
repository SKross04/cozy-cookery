import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
STONE=[(70,70,76),(112,112,118),(146,146,150),(176,176,180),(204,204,206)]
rnd=random.Random(6)
# mortar block texture: speckled stone, darker band at the bottom rows (used for the sides)
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        c=STONE[3] if rnd.random()<0.75 else STONE[2]
        if rnd.random()<0.06: c=STONE[4]
        if y>=15: c=STONE[2]
        px[x,y]=c+(255,)
img.save(f"{out}/block/mortar.png")
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        px[x,y]=(STONE[1] if rnd.random()<0.7 else STONE[2])+(255,)
img.save(f"{out}/block/mortar_inside.png")
# pestle (diagonal tool: stone head top-right, stone grip)
def pestle():
    fill={}; col={}
    for y in range(16):
        for x in range(16):
            u=x-y; v=x+y
            if -10<=u<=2 and v in (15,16):
                fill[(x,y)]=1; col[(x,y)]=STONE[4] if v==15 else STONE[3]
            if 3<=u<=9 and 14<=v<=17:
                fill[(x,y)]=1; col[(x,y)]=STONE[4] if v==14 else (STONE[2] if v==17 else STONE[3])
                if u==9: col[(x,y)]=STONE[2]
    for u in (-8,-7):                                  # twine grip
        for v in (15,16):
            if (u+v)%2==0: col[((u+v)//2,(v-u)//2)]=(168,140,92)
    img=new(); put(img,col); outline(img,fill,lambda p:STONE[0]); img.save(f"{out}/item/pestle.png")
pestle()
# flour: a little linen pouch tied with twine, flour spilling out
def flour():
    fill={}; col={}
    LIN=[(120,104,80),(196,180,150),(222,208,180),(240,230,206)]
    for y in range(16):
        for x in range(16):
            body=((x-7.5)/5.2)**2+((y-10.5)/4.2)**2<=1
            neck=5<=x<=10 and 4<=y<=6
            if body or neck:
                fill[(x,y)]=1; c=LIN[2]
                if x<=4 or y<=7: c=LIN[3]
                if x>=10 or y>=13: c=LIN[1]
                col[(x,y)]=c
    for x in range(5,11): col[(x,6)]=(168,128,72)       # twine
    for p in [(4,3),(5,3),(6,2),(7,2),(8,2),(9,2),(10,3),(11,3),(6,3),(7,3),(8,3),(9,3)]:   # flour on top
        col[p]=(252,250,244); fill[p]=1
    for p in [(13,14),(14,14),(14,13),(2,15)]: col[p]=(250,248,240); fill[p]=1
    img=new(); put(img,col); outline(img,fill,lambda p:LIN[0]); img.save(f"{out}/item/flour.png")
flour()

# ---- v2: chunkier pestle with a rounded head ----
def pestle():
    fill={}; col={}
    for y in range(16):
        for x in range(16):
            u=x-y; v=x+y
            if -10<=u<=1 and v in (15,16):
                fill[(x,y)]=1; col[(x,y)]=STONE[4] if v==15 else STONE[3]
            du=(u-6.5)/4.2; dv=(v-15.5)/2.9
            if du*du+dv*dv<=1:
                fill[(x,y)]=1
                col[(x,y)]=STONE[4] if v<=14 else (STONE[2] if v>=17 else STONE[3])
    for u in (-9,-8,-7):
        for v in (15,16):
            if (u+v)%2==0: col[((u+v)//2,(v-u)//2)]=(168,140,92)
    img=new(); put(img,col); outline(img,fill,lambda p:STONE[0]); img.save(f"{out}/item/pestle.png")
pestle()
