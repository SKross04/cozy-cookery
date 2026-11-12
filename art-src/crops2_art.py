import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
LEAF=[(40,72,30),(62,110,44),(92,148,58),(132,184,80)]
CAB=[(48,96,40),(86,148,62),(128,186,92),(178,214,136)]
STEM=(78,120,46)
def save(img,name): img.save(f"{out}/{name}.png")
# ---------- items ----------
def cabbage_item():
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            d=math.hypot((x-7.5)/6.4,(y-8.5)/5.8)
            if d<=1:
                fill[(x,y)]=1
                c=CAB[2] if d<0.55 else CAB[1]
                if (x-7.5)*0.6+(y-8.5)<-3.5: c=CAB[3]
                if abs(x-7.5)<0.8 and y>5: c=CAB[3]                 # middle vein
                if d>0.8 and (x+y)%3==0: c=CAB[0]                   # outer leaf folds
                col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p:CAB[0]); save(img,"item/cabbage")
def onion_item():
    col={}; fill={}
    O=[(110,58,22),(170,98,40),(204,136,62),(232,180,104)]
    for y in range(16):
        for x in range(16):
            d=math.hypot((x-7.5)/5.6,(y-9.5)/5.0)
            top= 2<=y<=5 and abs(x-7.5)<=1.2-(5-y)*0.2
            if d<=1 or top:
                fill[(x,y)]=1
                c=O[2]
                if x<6 and y<10: c=O[3]
                if x>9 or y>12: c=O[1]
                if d<=1 and (x in (5,10)) and 6<y<14: c=O[1]        # papery lines
                if top: c=O[1]
                col[(x,y)]=c
    for p in [(6,15),(7,15),(8,15),(9,15)]: col[p]=(214,196,150); fill[p]=1   # little roots
    img=new(); put(img,col); outline(img,fill,lambda p:O[0]); save(img,"item/onion")
def garlic_item():
    col={}; fill={}
    G=[(150,134,120),(220,210,196),(240,234,224),(252,250,244)]
    for y in range(16):
        for x in range(16):
            d=math.hypot((x-7.5)/5.8,(y-9.8)/4.8)
            top= 3<=y<=6 and abs(x-7.5)<=1.0
            if d<=1 or top:
                fill[(x,y)]=1
                c=G[2]
                if x<6 and y<10: c=G[3]
                if x>9 or y>12: c=G[1]
                if d<=1 and x in (5,8,11) and y>7: c=G[1]           # clove lines
                if d<=1 and x in (6,9) and y>11: c=(186,140,186)     # purple streaks
                if top: c=G[1]
                col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p:G[0]); save(img,"item/garlic")
def seeds(name, c1, c2, o):
    col={}; fill={}
    for (cx,cy) in [(5,6),(10,5),(7,10),(11,10),(4,11)]:
        for p in [(cx,cy),(cx+1,cy),(cx,cy+1),(cx+1,cy+1)]: col[p]=c1; fill[p]=1
        col[(cx,cy)]=c2
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
cabbage_item(); onion_item(); garlic_item()
seeds("cabbage_seeds",(92,64,46),(140,104,76),(52,34,24))
# ---------- crop stages (4 looks for 8 ages) ----------
def crop_stage(name, i, kind):
    rnd=random.Random(name+str(i)); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    if kind=="cabbage":
        size=[2,4,6,7][i]
        for cx in (4,11):
            for y in range(16-size,16):
                for x in range(16):
                    if abs(x-cx)<= (y-(16-size))*0.9+1 and abs(x-cx)<=size:
                        px[x,y]=(CAB[1] if (x+y)%3 else CAB[2])+(255,)
            if i==3:                                                   # the head
                for y in range(9,14):
                    for x in range(cx-3,cx+4):
                        if math.hypot(x-cx,(y-11.5)*1.2)<=3.2: px[x,y]=(CAB[3] if x<cx else CAB[2])+(255,)
    else:
        h=[3,6,9,11][i]
        for x in (2,4,6,9,11,13):
            hh=h-rnd.randint(0,2)
            for y in range(15,15-hh,-1): px[x,y]=(LEAF[2] if x%4==0 else LEAF[1])+(255,)
        if i==3:
            bulb=(204,136,62) if kind=="onion" else (240,234,224)
            for x in range(16):
                if x%5 in (1,2,3): px[x,15]=bulb+(255,); 
                if x%5==2: px[x,14]=bulb+(255,)
    save(img,f"block/{name}_stage{i}")
for k in ("cabbage","onion","garlic"):
    for i in range(4): crop_stage(f"{k}_crop",i,k)
# ---------- wild plants ----------
def wild(name, kind):
    rnd=random.Random(name); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    if kind=="cabbage":
        for y in range(8,16):
            for x in range(16):
                if abs(x-7.5)<=(y-8)*1.1+1 and rnd.random()<0.85: px[x,y]=rnd.choice(CAB[:3])+(255,)
    else:
        for x in (3,5,7,8,10,12):
            hh=rnd.randint(7,11)
            for y in range(15,15-hh,-1): px[x,y]=rnd.choice(LEAF[1:3])+(255,)
        flower=(178,120,206) if kind=="onion" else (246,246,240)    # onion: purple ball, garlic: white umbel
        for (cx,cy) in [(5,3),(10,5)]:
            for dx in (-1,0,1):
                for dy in (-1,0,1):
                    if abs(dx)+abs(dy)<2 or rnd.random()<0.5: px[cx+dx,cy+dy]=flower+(255,)
            for y in range(cy+2,12): px[cx,y]=STEM+(255,)
    save(img,f"block/{name}")
wild("wild_cabbage","cabbage"); wild("wild_onion","onion"); wild("wild_garlic","garlic")

# ---- garlic clove ----
def garlic_clove():
    col={}; fill={}
    G=[(150,134,120),(220,210,196),(240,234,224),(252,250,244)]
    for y in range(16):
        for x in range(16):
            # a teardrop: wide at the bottom, pointed at the top, slightly curved
            t=(y-3)/10.0
            if 0<=t<=1:
                half=3.6*math.sin(t*math.pi*0.85)+0.3
                cx=7.5+1.2*(1-t)
                if abs(x-cx)<=half:
                    fill[(x,y)]=1
                    c=G[2]
                    if x<cx-1: c=G[3]
                    if x>cx+1.5: c=G[1]
                    if y>=12 and x>cx: c=(196,160,196)
                    col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p:G[0]); img.save(f"{out}/item/garlic_clove.png")
garlic_clove()
