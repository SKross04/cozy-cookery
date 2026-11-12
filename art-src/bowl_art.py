import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
W=[(86,50,24),(150,98,54),(186,130,74),(214,162,102),(234,194,136)]
rnd=random.Random(8)
# mixing bowl side: wooden staves with a darker rim and a copper band
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        c=W[3] if (x//3)%2==0 else W[2]
        if x%3==2: c=W[1]
        if y<=10: c=W[4] if y==10 else c
        if y==12: c=(204,124,72)            # copper band
        if y>=15: c=W[1]
        px[x,y]=c+(255,)
img.save(f"{out}/block/mixing_bowl.png")
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        px[x,y]=(W[3] if rnd.random()<0.8 else W[2])+(255,)
img.save(f"{out}/block/mixing_bowl_inside.png")
# ---------- doughs ----------
def blob(name, base, light, dark, o, extras=(), shape=lambda x,y: ((x-7.5)/6.0)**2+((y-9)/4.6)**2<=1):
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            if shape(x,y):
                fill[(x,y)]=1
                col[(x,y)] = light if (x+y)<13 else (dark if (x+y)>20 else base)
    for p,c in extras: col[p]=c; fill[p]=1
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/item/{name}.png")
DOUGH=((236,222,186),(248,240,214),(212,194,150),(150,128,90))
blob("bread_dough",*DOUGH, extras=[((5,7),(212,194,150)),((6,8),(212,194,150)),((8,7),(212,194,150)),((9,8),(212,194,150)),((11,7),(212,194,150))])
chips=[((x,y),(86,50,30)) for x,y in [(5,8),(8,7),(10,10),(6,11),(11,8),(8,11)]]
blob("cookie_dough",(226,196,140),(240,214,164),(200,168,112),(132,100,60),extras=chips,
     shape=lambda x,y: math.hypot(x-7.5,y-9)<=5.6)
def tin(name, fill_c, fill_l, crust=None, lattice=False):
    col={}; fill={}
    CU=[(110,56,32),(164,88,50),(204,124,72),(234,164,108)]
    for y in range(16):
        for x in range(16):
            top=((x-7.5)/6.8)**2+((y-7.5)/3.2)**2<=1
            side=7.5<=y<=11.5 and abs(x-7.5)<=6.8-(y-7.5)*0.35
            if side: fill[(x,y)]=1; col[(x,y)]=CU[2] if x<9 else CU[1]
            if top:
                fill[(x,y)]=1
                rim=((x-7.5)/6.8)**2+((y-7.5)/3.2)**2>0.6
                if crust and rim: col[(x,y)]=crust
                elif rim: col[(x,y)]=CU[3]
                else:
                    col[(x,y)]=fill_l if (x+y)%4==0 else fill_c
                    if lattice and (x%3==0 or (x+y)%5==0): col[(x,y)]=crust
    img=new(); put(img,col); outline(img,fill,lambda p:CU[0]); img.save(f"{out}/item/{name}.png")
tin("cake_batter",(244,226,176),(252,240,206))
tin("raw_pumpkin_pie",(232,138,44),(244,170,80),crust=(240,228,196))
tin("raw_apple_pie",(232,214,156),(246,232,186),crust=(240,228,196),lattice=True)
