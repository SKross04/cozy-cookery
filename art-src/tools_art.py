import sys, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
out=sys.argv[1]
METAL = {  # outline, dark, mid, light, edge
 "iron":      ((58,62,74),(140,148,162),(190,197,208),(226,231,238),(252,253,255)),
 "golden":    ((112,62,12),(206,146,30),(240,190,58),(255,226,118),(255,247,196)),
 "diamond":   ((16,84,90),(38,166,168),(90,216,210),(168,244,236),(228,255,251)),
 "netherite": ((30,24,28),(64,54,60),(90,78,84),(122,108,112),(164,150,152)),
 "flint":     ((26,26,32),(58,58,68),(84,84,96),(118,118,130),(160,160,172)),
}
WOOD = {  # outline, dark, mid, light
 "iron":      ((84,48,22),(140,88,46),(176,120,66),(206,154,96)),     # oak
 "golden":    ((52,30,18),(92,56,34),(118,74,46),(146,98,64)),        # walnut
 "diamond":   ((112,92,62),(196,170,124),(222,200,152),(240,224,184)),# birch
 "netherite": ((54,14,24),(110,34,50),(140,48,66),(170,74,90)),       # crimson
 "flint":     ((84,48,22),(140,88,46),(176,120,66),(206,154,96)),
}
TWINE = ((92,70,40),(168,140,92),(204,180,128))
BRASS = ((96,64,20),(196,150,62),(236,200,110))
def knife(name):
    o,d,m,l,e = METAL[name]; wo,wd,wm,wl = WOOD[name]
    fill={}; col={}
    rnd=random.Random(name)
    for y in range(16):
        for x in range(16):
            u=x-y; v=x+y
            vmax = 18 if u<=6 else 17 if u<=9 else 16 if u<=11 else 15 if u<=12 else -1
            if -11<=u<=-3 and v in (15,16):           # handle
                fill[(x,y)]="h"
                if name=="flint":
                    col[(x,y)] = TWINE[2] if (u//2)%2==0 else TWINE[1]
                else:
                    col[(x,y)] = wl if v==15 else wm
                    if u in (-9,-5) and v==15: col[(x,y)] = BRASS[2]
                    if u==-11: col[(x,y)] = wd
            elif u in (-2,-1) and 14<=v<=17:          # bolster
                fill[(x,y)]="b"
                if name=="flint":
                    col[(x,y)] = TWINE[1] if v in (14,17) else TWINE[2]
                else:
                    col[(x,y)] = d if v in (14,17) else m
            elif 0<=u<=12 and 15<=v<=vmax:            # blade
                fill[(x,y)]="k"
                if v==15: c=l
                elif v==vmax: c=e
                else: c=m
                if name=="flint" and v!=vmax and rnd.random()<0.35: c = d if rnd.random()<0.5 else l
                if u==0 and v>15: c=d
                col[(x,y)]=c
    img=new(); put(img,col)
    outline(img, fill, lambda p: wo if p=="h" else (TWINE[0] if name=="flint" and p=="b" else o))
    img.save(f"{out}/{name}_knife.png")
for n in METAL: knife(n)

# wooden spoon
fill={}; col={}
wo,wd,wm,wl,wh = (86,50,24),(146,94,50),(184,128,72),(212,160,100),(234,194,136)
for y in range(16):
    for x in range(16):
        u=x-y; v=x+y
        if -11<=u<=1 and v in (15,16):
            fill[(x,y)]="h"; col[(x,y)] = wl if v==15 else wm
            if u in (-9,-8): col[(x,y)] = wd            # carved ring
        du=(u-6.5)/5.0; dv=(v-15.5)/3.2
        if du*du+dv*dv<=1.0:
            fill[(x,y)]="s"
            r=du*du+dv*dv
            if r<0.45: col[(x,y)] = wd if (v>=15.5) else wm   # hollow of the bowl
            elif v<=14 or u>=10: col[(x,y)] = wh              # rim highlight
            else: col[(x,y)] = wl
img=new(); put(img,col); outline(img, fill, lambda p: wo); img.save(f"{out}/wooden_spoon.png")
