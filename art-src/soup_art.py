import sys, math, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
WOOD=[(86,50,24),(146,94,50),(184,128,72),(214,162,100)]
SOUPS={ # dark, mid, light, bits
 "beef_stew":((120,52,26),(160,78,38),(196,112,58),[(232,128,40),(150,40,40),(240,214,150)]),
 "chicken_soup":((196,150,60),(226,188,92),(246,222,140),[(232,128,40),(250,236,200),(120,160,60)]),
 "burnt_stew":((28,22,20),(46,36,32),(70,56,48),[(20,16,14),(90,70,56)]),
}
def bowl(name, pal):
    d,m,l,bits=pal; rnd=random.Random(name); fill={}; col={}
    for y in range(16):
        for x in range(16):
            top=((x-7.5)/6.6)**2+((y-6.5)/2.6)**2<=1       # soup surface (ellipse)
            body= 6<=y<=13 and abs(x-7.5)<=6.6-max(0,(y-7))*0.75
            if body and not top:
                fill[(x,y)]=1; c=WOOD[2]
                if x<=4: c=WOOD[3]
                if x>=11 or y==13: c=WOOD[1]
                if y==9 and x%3==0: c=WOOD[1]                  # carved line
                col[(x,y)]=c
            if top:
                fill[(x,y)]=1
                rim=((x-7.5)/6.6)**2+((y-6.5)/2.6)**2>0.62
                if rim: col[(x,y)]=WOOD[3] if y<=6 else WOOD[2]
                else:
                    c=m if (x+y)%3 else l
                    if y>=7: c=d if x>9 else m
                    if rnd.random()<0.25: c=rnd.choice(bits)
                    col[(x,y)]=c
    # steam wisps (not for burnt: smoke instead)
    wisp=(236,236,240) if name!="burnt_stew" else (90,90,96)
    for p in [(6,2),(6,3),(7,1),(9,3),(9,2),(10,1)]: col[p]=wisp; fill[p]=2
    img=new(); put(img,col); outline(img,{k:v for k,v in fill.items() if v==1},lambda p:WOOD[0]); img.save(f"{out}/item/{name}.png")
for n,p in SOUPS.items(): bowl(n,p)
# burnt pot liquid (animated, slow)
B=[(22,18,16),(38,30,26),(58,46,38),(96,60,30)]
img=Image.new("RGBA",(16,128)); px=img.load(); rnd=random.Random(3)
for f in range(8):
    for y in range(16):
        for x in range(16):
            v=math.sin(x*0.7+f*0.785)+math.sin(y*0.6-f*0.785)
            c=B[2] if v>1.3 else B[1] if v>-0.4 else B[0]
            if rnd.random()<0.03: c=B[3]
            px[x,y+16*f]=c+(255,)
img.save(f"{out}/block/cooking_pot_burnt.png")
open(f"{out}/block/cooking_pot_burnt.png.mcmeta","w").write('{\n  "animation": {\n    "frametime": 6,\n    "interpolate": true\n  }\n}\n')
