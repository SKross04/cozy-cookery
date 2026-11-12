import sys, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
WOOD=[(86,50,24),(150,98,54),(186,130,74),(214,162,100)]
LEAF=[(40,72,30),(62,110,44),(92,148,58),(132,184,80)]
STEM=(78,120,46)
def lattice(top=False):
    img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    for y in range(16):
        for x in range(16):
            post = x in (1,14)
            diag = (x+y)%8==0 or (x-y)%8==0
            if post: px[x,y]=(WOOD[2] if x==1 else WOOD[1])+(255,)
            elif diag and 1<x<14: px[x,y]=WOOD[3 if (x+y)%8==0 else 2]+(255,)
            if top and y in (0,1): px[x,y]=(WOOD[3] if y==0 else WOOD[1])+(255,)
    return img
lattice().save(f"{out}/block/trellis_lower.png")
lattice(top=True).save(f"{out}/block/trellis_upper.png")
# item icon: the lattice panel in the inventory
icon=lattice(top=True); ipx=icon.load()
for y in range(16):
    for x in range(16):
        if ipx[x,y][3] and (x in (1,14) and y==15): ipx[x,y]=WOOD[0]+(255,)
icon.save(f"{out}/item/trellis.png")
def plant(name, seed, bottom_height, leaves, flowers=0, fruit=None, nfruit=0, tendrils=False):
    rnd=random.Random(name+seed); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    for x in (4,5,10,11):                                   # vine stems climbing up
        for y in range(15,15-bottom_height,-1):
            if rnd.random()<0.9: px[x+((15-y)//5)%2,y]=STEM+(255,)
    for _ in range(leaves):
        x=rnd.randint(1,14); y=rnd.randint(16-bottom_height,15)
        for dx,dy in [(0,0),(1,0),(0,1),(-1,0),(0,-1)]:
            if 0<=x+dx<16 and 0<=y+dy<16 and rnd.random()<0.85: px[x+dx,y+dy]=rnd.choice(LEAF[1:])+(255,)
    if tendrils:
        for x in (3,12):
            for y in range(rnd.randint(8,12),16): px[x,y]=STEM+(255,)
    for _ in range(flowers):
        x=rnd.randint(1,14); y=rnd.randint(max(0,16-bottom_height),13); px[x,y]=(246,214,64,255)
    for _ in range(nfruit):
        x=rnd.randint(1,13); y=rnd.randint(max(1,16-bottom_height+1),13)
        for dx,dy in [(0,0),(1,0),(0,1),(1,1)]: px[x+dx,y+dy]=fruit[0]+(255,)
        px[x,y]=fruit[1]+(255,)
    img.save(f"{out}/block/{name}.png")
GREEN=((96,150,54),(150,196,90)); RED=((206,40,38),(246,120,100))
# bottom half: 5 looks
plant("tomato_trellis_lower_stage0","a",5,3)
plant("tomato_trellis_lower_stage1","b",12,14)
plant("tomato_trellis_lower_stage2","c",16,24,flowers=5)
plant("tomato_trellis_lower_stage3","d",16,24,fruit=GREEN,nfruit=4)
plant("tomato_trellis_lower_stage4","e",16,24,fruit=RED,nfruit=5)
# top half: stage 0 has no plant (only the lattice)
plant("tomato_trellis_upper_stage1","f",6,4,tendrils=True)
plant("tomato_trellis_upper_stage2","g",14,18,flowers=4)
plant("tomato_trellis_upper_stage3","h",14,18,fruit=GREEN,nfruit=3)
plant("tomato_trellis_upper_stage4","i",14,18,fruit=RED,nfruit=4)
