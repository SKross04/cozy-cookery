import sys, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
exec(open("/home/claude/scripts/soup_art.py").read().split("def bowl")[0].split("out=sys.argv[1]")[1])
out=sys.argv[1]
def bowl(name, pal):
    d,m,l,bits=pal; rnd=random.Random(name); fill={}; col={}
    for y in range(16):
        for x in range(16):
            rim =((x-7.5)/6.9)**2+((y-6.5)/2.9)**2<=1
            soup=((x-7.5)/5.7)**2+((y-6.6)/2.0)**2<=1
            body= y>=6.5 and ((x-7.5)/6.9)**2+((y-6.5)/6.3)**2<=1 and y<=12
            foot= y in (12,13) and abs(x-7.5)<=3
            if body or foot:
                fill[(x,y)]=1; c=WOOD[2]
                if x<=3: c=WOOD[3]
                if x>=12 or y>=12: c=WOOD[1]
                if y==10 and 3<=x<=12: c=WOOD[1] if x%2 else WOOD[2]   # carved band
                col[(x,y)]=c
            if rim:
                fill[(x,y)]=1; col[(x,y)]=WOOD[3] if y<=6 else WOOD[2]
            if soup:
                c=l if y<=5 else m
                if x>=10 and y>=7: c=d
                if rnd.random()<0.22: c=rnd.choice(bits)
                col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p:WOOD[0])
    px=img.load(); wisp=(240,240,244) if name!="burnt_stew" else (96,96,104)
    for (x,y),a in {(6,3):170,(5,2):120,(6,1):70,(9,3):170,(10,2):120,(9,1):70}.items():
        px[x,y]=wisp+(a,)
    img.save(f"{out}/item/{name}.png")
for n,p in SOUPS.items(): bowl(n,p)
