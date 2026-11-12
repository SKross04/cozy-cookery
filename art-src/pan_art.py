import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
IRON=[(30,28,34),(46,44,52),(62,60,70),(84,82,94),(112,110,122)]
COPPER=[(110,56,32),(164,88,50),(204,124,72),(234,164,108)]
KRAFT=((118,88,54),(186,152,106),(212,182,136),(232,208,166))
def sh(c,f): return tuple(max(0,min(255,int(v*f))) for v in c)
# ---------- shared shapes ----------
def strips(name,o,d,m,l,f,char=None):
    fill={}; col={}
    for k,(base,ulo,uhi) in enumerate([(9,-5,6),(14,-7,8),(19,-6,5)]):
        for y in range(16):
            for x in range(16):
                u=x-y; v=x+y
                if ulo<=u<=uhi and v in (base,base+1,base+2):
                    fill[(x,y)]=k
                    col[(x,y)] = l if v==base else (d if v==base+2 else m)
                    if v==base+1 and (u+k*3)%6==0: col[(x,y)]=f
                    if char and (u+k)%4==0 and v!=base: col[(x,y)]=char   # grill / crisp marks
                    if u in (ulo,uhi): col[(x,y)]=d
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/{name}.png")
def cubes(name,o,d,m,l,fleck):
    col={}
    for cx,cy in [(5,3),(9,5),(2,7),(7,9),(11,10)]:
        for dx in range(-1,5):
            for dy in range(-1,5): col[(cx+dx,cy+dy)]=o
        for dx in range(4):
            for dy in range(4):
                c = l if dy==0 else (d if dx==3 or dy==3 else m)
                if (dx,dy)==(1,1) and fleck: c=fleck
                col[(cx+dx,cy+dy)]=c
    img=new(); put(img,col); img.save(f"{out}/{name}.png")
def mince(name,o,d,m,l,f):
    rnd=random.Random(name); fill={}; col={}
    for y in range(16):
        for x in range(16):
            if 1<=x<=14 and 9<=y<=14 and not ((x in (1,14)) and (y in (9,14))):
                fill[(x,y)]="p"; col[(x,y)] = KRAFT[3] if y==9 else (KRAFT[1] if y==14 else KRAFT[2])
                if x==7 and y>9: col[(x,y)]=KRAFT[1]
    for y in range(16):
        for x in range(16):
            if ((x-7.5)/5.6)**2+((y-8.2)/4.2)**2<=1 and y<=11:
                fill[(x,y)]="m"; r=rnd.random()
                col[(x,y)] = l if (y<=6 and r<0.5) else (d if r<0.3 else m)
                if rnd.random()<0.1: col[(x,y)]=f
    img=new(); put(img,col); outline(img,fill,lambda p: KRAFT[0] if p=="p" else o); img.save(f"{out}/{name}.png")
def coins(name,o,rim,flesh,core):
    col={}
    for (cx,cy) in [(4,4.5),(11,4),(4.5,11),(11.5,11)]:
        for y in range(16):
            for x in range(16):
                dd=math.hypot(x-cx,y-cy)
                if dd<=3.7: col[(x,y)]=o
        for y in range(16):
            for x in range(16):
                dd=math.hypot(x-cx,y-cy)
                if dd<=2.7: col[(x,y)] = rim if dd>1.8 else (core if dd<0.9 else flesh)
    img=new(); put(img,col); img.save(f"{out}/{name}.png")
# ---------- seared / fried meat ----------
BEEF=((70,34,18),(112,54,28),(146,78,40),(178,108,58),(214,170,120))
PORK=((92,30,26),(150,52,40),(186,78,56),(214,112,80),(246,224,196))
MUTTON=((66,30,22),(104,52,34),(136,76,48),(168,104,66),(220,190,150))
for n,(o,d,m,l,f) in {"beef":BEEF,"mutton":MUTTON}.items():
    strips(f"seared_{n}_strips",o,d,m,l,f,char=sh(d,0.7))
    cubes(f"seared_{n}_cubes",o,d,m,l,f)
    mince(f"browned_ground_{n}",o,d,m,l,f)
strips("bacon",*PORK,char=None)
cubes("fried_pork_cubes",*PORK[:4],None)
mince("browned_ground_pork",*PORK)
# ---------- potatoes, mushrooms ----------
coins("potato_chips",(130,82,22),(206,146,48),(240,196,90),(250,222,132))
cubes("fried_potato_cubes",(130,82,22),(196,134,42),(232,184,80),(250,216,124),(176,110,30))
def fried_mushrooms():
    col={}; fill={}
    shape=["..CCCC..",".CccccC.","CccccccC","CffffffC",".FffffF.","..ffff..","..fFff..","..ffff..","..FFFF.."]
    for (ox,oy) in [(1,1),(7,6)]:
        for y,row in enumerate(shape):
            for x,ch in enumerate(row):
                if ch=='.': continue
                col[(ox+x,oy+y)]={'C':(96,58,30),'c':(132,84,44),'f':(214,170,104),'F':(178,128,70)}[ch]; fill[(ox+x,oy+y)]=1
    img=new(); put(img,col); outline(img,fill,lambda p:(64,38,20)); img.save(f"{out}/fried_mushrooms.png")
fried_mushrooms()
# ---------- fried egg ----------
def fried_egg():
    rnd=random.Random(1); col={}; fill={}
    for y in range(16):
        for x in range(16):
            r=math.hypot((x-7.5)/1.0,(y-8)/0.85)+0.9*math.sin(math.atan2(y-8,x-7.5)*3)
            if r<=6.4:
                fill[(x,y)]=1; col[(x,y)]=(250,248,240) if r<5.6 else (232,214,180)
                if r>5.0 and rnd.random()<0.3: col[(x,y)]=(214,170,104)   # crispy edge
    for y in range(16):
        for x in range(16):
            d=math.hypot(x-8,y-7.5)
            if d<=2.4: col[(x,y)]=(252,196,40) if d>1.0 else (255,228,120)
    col[(7,6)]=(255,244,190)
    img=new(); put(img,col); outline(img,fill,lambda p:(176,150,110)); img.save(f"{out}/fried_egg.png")
fried_egg()
# ---------- charred scraps ----------
def charred():
    rnd=random.Random(5); col={}; fill={}
    for (cx,cy,r) in [(5,6,3.2),(10,8,3.6),(6,11,2.6)]:
        for y in range(16):
            for x in range(16):
                if math.hypot(x-cx,y-cy)<=r:
                    fill[(x,y)]=1; c=(34,28,26) if rnd.random()<0.7 else (56,46,42)
                    if rnd.random()<0.05: c=(220,90,30)                     # last embers
                    col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p:(14,10,10)); img.save(f"{out}/charred_scraps.png")
charred()
# ---------- wooden spatula ----------
def spatula():
    wo,wd,wm,wl,wh=(86,50,24),(146,94,50),(184,128,72),(212,160,100),(234,194,136)
    fill={}; col={}
    for y in range(16):
        for x in range(16):
            u=x-y; v=x+y
            if -11<=u<=1 and v in (15,16):
                fill[(x,y)]=1; col[(x,y)]= wl if v==15 else wm
                if u in (-9,-8): col[(x,y)]=wd
            if 2<=u<=11 and 13<=v<=18:                      # flat blade
                fill[(x,y)]=1; col[(x,y)]= wh if v==13 else (wd if v==18 else wl)
                if v in (15,16) and u in (5,6,8,9): col.pop((x,y)); fill.pop((x,y))   # slots
    img=new(); put(img,col); outline(img,fill,lambda p:wo); img.save(f"{out}/wooden_spatula.png")
spatula()
# ---------- frying pan: item icon (top view) ----------
def pan_icon():
    fill={}; col={}
    for y in range(16):
        for x in range(16):
            d=math.hypot(x-6.5,y-8.5)
            if d<=6.2:
                fill[(x,y)]=1
                col[(x,y)] = IRON[3] if d>5.2 else (IRON[1] if d>4.4 else IRON[2])
                if d<=4.4 and (x+y)%5==0: col[(x,y)]=IRON[3]           # seasoning sheen
                if d>5.2 and x<6 and y<8: col[(x,y)]=IRON[4]
            if 12<=x<=15 and 7<=y<=9:
                fill[(x,y)]=1; col[(x,y)]=COPPER[3] if y==7 else (COPPER[1] if y==9 else COPPER[2])
    img=new(); put(img,col); outline(img,fill,lambda p:IRON[0]); img.save(f"{out}/frying_pan.png")
pan_icon()

# ---- v2 tweaks ----
PORK_COOKED=((92,44,26),(146,82,50),(182,112,70),(210,146,98),(240,214,180))
cubes("fried_pork_cubes",*PORK_COOKED[:4],None)
mince("browned_ground_pork",*PORK_COOKED)
def fried_egg():
    rnd=random.Random(1); col={}; fill={}
    for y in range(16):
        for x in range(16):
            a=math.atan2(y-8,x-7.5)
            r=math.hypot(x-7.5,(y-8)/0.9)+0.45*math.sin(a*5+0.6)
            if r<=6.3:
                fill[(x,y)]=1; col[(x,y)]=(250,248,240) if r<5.4 else (232,214,180)
                if r>5.0 and rnd.random()<0.25: col[(x,y)]=(214,170,104)
    for y in range(16):
        for x in range(16):
            d=math.hypot(x-8.5,y-7)
            if d<=2.6: col[(x,y)]=(250,190,36) if d>1.2 else (255,224,110)
    col[(7,6)]=(255,246,200)
    img=new(); put(img,col); outline(img,fill,lambda p:(176,150,110)); img.save(f"{out}/fried_egg.png")
fried_egg()
def spatula():
    wo,wd,wm,wl,wh=(86,50,24),(146,94,50),(184,128,72),(212,160,100),(234,194,136)
    fill={}; col={}
    for y in range(16):
        for x in range(16):
            u=x-y; v=x+y
            if -11<=u<=0 and v in (15,16):
                fill[(x,y)]=1; col[(x,y)]= wl if v==15 else wm
                if u in (-9,-8): col[(x,y)]=wd
            if 1<=u<=12 and 12<=v<=19:                       # wide flat blade
                fill[(x,y)]=1; col[(x,y)]= wh if v==12 else (wd if v==19 else wl)
                if u==1: col[(x,y)]=wd
    for (u,v) in [(5,15),(6,15),(7,15),(5,16),(6,16),(7,16),(9,15),(10,15),(9,16),(10,16)]:   # two slots
        x=(u+v)//2; y=(v-u)//2
        if (u+v)%2==0: col.pop((x,y),None); fill.pop((x,y),None)
    img=new(); put(img,col); outline(img,fill,lambda p:wo); img.save(f"{out}/wooden_spatula.png")
spatula()
