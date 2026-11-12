import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
def cubes(name, o,d,m,l,fleck):
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
def rounds(name, o, rim, flesh, core, centers, r=3.2):
    col={}
    for (cx,cy) in centers:                     # back to front
        for y in range(16):
            for x in range(16):
                dd=math.hypot(x-cx,y-cy)
                if dd<=r+1.0: col[(x,y)]=o
        for y in range(16):
            for x in range(16):
                dd=math.hypot(x-cx,y-cy)
                if dd<=r:
                    c = rim if dd>r-1.0 else (core if dd<1.3 else flesh)
                    if dd<=r and x<cx and y<cy and dd>r-1.0: c=tuple(min(255,int(v*1.12)) for v in rim)
                    col[(x,y)]=c
    img=new(); put(img,col); img.save(f"{out}/{name}.png")
# carrots
rounds("carrot_slices",(132,52,10),(214,104,24),(240,140,40),(252,184,92),[(4.5,4.5),(10.5,6),(6,10.5),(11,11.5)])
cubes("carrot_cubes",(132,52,10),(196,90,20),(236,132,36),(252,176,84),None)
# potatoes (skin at the rim)
rounds("potato_slices",(110,78,36),(176,134,72),(236,214,140),(244,226,160),[(4.5,4.5),(10.5,6),(6,10.5),(11,11.5)])
cubes("potato_cubes",(110,78,36),(202,176,104),(234,212,138),(248,232,170),(176,134,72))
# beetroot
cubes("beetroot_cubes",(76,10,30),(122,18,48),(164,30,66),(204,64,100),(222,120,150))
# sliced mushrooms: one brown-capped and one red-capped slice
def mushroom_slice(col, ox, oy, cap, capl):
    flesh=(238,226,200); fleshd=(208,192,160); o=(96,70,52)
    shape=["..CCCC..",".CccccC.","CccccccC","CffffffC",".FffffF.","..ffff..","..fFff..","..ffff..","..FFFF.."]
    for y,row in enumerate(shape):
        for x,ch in enumerate(row):
            if ch=='.': continue
            col[(ox+x,oy+y)]={'C':cap,'c':capl,'f':flesh,'F':fleshd}[ch]
    return o
def sliced_mushrooms():
    col={}; fill={}
    for (ox,oy,cap,capl) in [(1,1,(112,74,48),(150,104,70)),(7,6,(168,30,30),(206,56,50))]:
        before=set(col); o=mushroom_slice(col,ox,oy,cap,capl)
        for p in set(col)-before: fill[p]=1
    img=new(); put(img,col); outline(img,fill,lambda p:(70,46,32)); img.save(f"{out}/sliced_mushrooms.png")
sliced_mushrooms()
# apple slices: two wedges with red skin and seeds
def apple_slices():
    col={}; fill={}
    for (cx,cy,rot) in [(6,7,0),(10,10,1)]:
        for y in range(16):
            for x in range(16):
                dx,dy=x-cx,y-cy
                if rot: dx,dy=-dy,dx
                if dy>=0 and math.hypot(dx,dy)<=4.6:
                    fill[(x,y)]=1
                    dd=math.hypot(dx,dy)
                    col[(x,y)]=(184,30,34) if dd>3.8 else ((246,236,196) if dd>1.2 else (232,214,160))
        col[(cx,cy+1)]=(70,40,20)
    img=new(); put(img,col); outline(img,fill,lambda p:(110,20,24)); img.save(f"{out}/apple_slices.png")
apple_slices()
# pumpkin slice: a wedge with orange skin, pale flesh and seeds
def pumpkin_slice():
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            dx,dy=x-7.5,y-4
            r=math.hypot(dx,dy)
            ang=math.degrees(math.atan2(dy,dx))
            if 2.5<=r<=10.5 and 45<=ang<=135:
                fill[(x,y)]=1
                col[(x,y)]=(214,112,22) if r>9.3 else ((240,160,56) if r>8.3 else (248,196,110))
                if r<4.0: col[(x,y)]=(236,176,84)
    for p in [(6,9),(9,10),(7,11)]: col[p]=(250,240,210)
    img=new(); put(img,col); outline(img,fill,lambda p:(132,62,10)); img.save(f"{out}/pumpkin_slice.png")
pumpkin_slice()
# soup layers for the vanilla bowl
AREA = {6:range(5,11), 7:range(3,13), 8:range(3,13), 9:list(range(5,11))+[12], 10:[10,11]}
for name,(d,m,l,chunks) in {
    "vegetable_soup":((104,112,46),(138,150,66),(172,184,96),[(236,132,36),(164,30,66),(236,214,140),(150,104,70)]),
    "pumpkin_soup":((196,104,24),(226,138,44),(246,176,84),[(250,236,200),(120,160,60)]),
}.items():
    rnd=random.Random(name); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    for y,xs in AREA.items():
        for x in xs:
            c = l if y==6 or (y==7 and x<8) else m
            if y>=9 or x>=11: c=d
            if rnd.random()<0.25: c=rnd.choice(chunks)
            px[x,y]=c+(255,)
    img.save(f"{out}/{name}.png")

# ---- v2: separate coins, clearer apple and pumpkin wedges ----
COINS=[(4,4.5),(11,4),(4.5,11),(11.5,11)]
rounds("carrot_slices",(132,52,10),(214,104,24),(240,140,40),(252,184,92),COINS,r=2.7)
rounds("potato_slices",(110,78,36),(176,134,72),(236,214,140),(248,232,176),COINS,r=2.7)
def wedge(name, centre, a0, a1, r0, r1, skin, skin_l, flesh, core, seed, o, seeds):
    col={}; fill={}
    cx,cy=centre
    for y in range(16):
        for x in range(16):
            r=math.hypot(x-cx,y-cy); ang=math.degrees(math.atan2(y-cy,x-cx))
            if r0<=r<=r1 and a0<=ang<=a1:
                fill[(x,y)]=1
                col[(x,y)] = skin if r>r1-1.1 else (skin_l if r>r1-2.0 else (flesh if r>r0+1.5 else core))
    for p in seeds: col[p]=seed
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/{name}.png")
wedge("apple_slices",(7.5,3),40,140,1.5,10.5,(184,30,34),(214,64,56),(248,238,200),(236,220,170),(84,46,22),(110,20,24),[(7,6),(8,7)])
wedge("pumpkin_slice",(7.5,-2),40,140,8.0,15.5,(196,96,16),(232,140,40),(248,190,104),(240,170,80),(252,242,214),(124,58,8),[(6,9),(9,9),(8,10)])
