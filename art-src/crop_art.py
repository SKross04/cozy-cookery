import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
LEAF=[(40,72,30),(62,110,44),(92,148,58),(132,184,80)]
STEM=(78,120,46)
def tomato_item():
    col={}; fill={}
    R=[(120,20,22),(186,34,34),(222,62,52),(246,128,104)]
    for y in range(16):
        for x in range(16):
            d=((x-7.5)/6.0)**2+((y-9)/5.4)**2
            if d<=1:
                fill[(x,y)]=1
                c=R[2]
                if x+y>19: c=R[1]
                if d>0.75 and x+y>17: c=R[1]
                col[(x,y)]=c
    for p in [(5,6),(5,7),(6,6)]: col[p]=R[3]
    for p in [(6,4),(7,4),(8,4),(9,4),(5,5),(10,5),(7,3),(8,3),(8,2)]: col[p]=LEAF[2]; fill[p]=1
    col[(8,1)]=LEAF[1]; fill[(8,1)]=1
    img=new(); put(img,col); outline(img,fill,lambda p:R[0] if col.get(p,(0,0,0))[0]>150 else LEAF[0]); img.save(f"{out}/item/tomato.png")
def seeds(name, c1, c2, o):
    col={}; fill={}
    for (cx,cy) in [(5,6),(10,5),(7,10),(11,10),(4,11)]:
        for p in [(cx,cy),(cx+1,cy),(cx,cy+1),(cx+1,cy+1)]: col[p]=c1; fill[p]=1
        col[(cx,cy)]=c2
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/item/{name}.png")
# crop stages: 4 textures for the 8 ages, drawn as plants seen from the side (crop model shows 4 planes)
def plant(img_px, rnd, height, leaves, fruit=None, flower=False):
    for x in range(16):
        if x%4 in (1,2):
            h=height-rnd.randint(0,2)
            for y in range(15,15-h,-1):
                img_px[x,y]=STEM+(255,) if x%4==1 else LEAF[1]+(255,)
    for _ in range(leaves):
        x=rnd.randint(0,15); y=rnd.randint(16-height,15)
        for dx,dy in [(0,0),(1,0),(0,1),(-1,0)]:
            if 0<=x+dx<16 and 0<=y+dy<16: img_px[x+dx,y+dy]=rnd.choice(LEAF[1:])+(255,)
    if flower:
        for _ in range(4):
            x=rnd.randint(1,14); y=rnd.randint(16-height,12); img_px[x,y]=(246,214,64,255)
    if fruit:
        for _ in range(5):
            x=rnd.randint(1,13); y=rnd.randint(16-height+2,13)
            for dx,dy in [(0,0),(1,0),(0,1),(1,1)]: img_px[x+dx,y+dy]=fruit[0]+(255,)
            img_px[x,y]=fruit[1]+(255,)
def stages(name, heights, leaves, fruit_stage_color, flower_stage=2):
    for i,(h,l) in enumerate(zip(heights,leaves)):
        rnd=random.Random(name+str(i)); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
        plant(px,rnd,h,l,fruit_stage_color if i==3 else None, flower=(i==flower_stage))
        img.save(f"{out}/block/{name}_stage{i}.png")
def wild(name, fruit):
    rnd=random.Random("wild"+name); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    plant(px,rnd,9,26,fruit)
    for x in range(16):                                   # sprawling, low to the ground
        if rnd.random()<0.6: px[x,15]=rnd.choice(LEAF[:3])+(255,)
    img.save(f"{out}/block/{name}.png")
tomato_item()
seeds("tomato_seeds",(232,214,150),(250,240,196),(150,120,70))
stages("tomato_crop",[3,7,11,13],[2,8,16,20],((206,40,38),(246,120,100)))
wild("wild_tomato",((206,40,38),(246,120,100)))
