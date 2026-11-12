# Soup layers drawn on top of the vanilla bowl (minecraft:item/bowl as layer0).
# The soup area matches the opening of the vanilla bowl.
import sys, random
from PIL import Image
out=sys.argv[1]
AREA = {6:range(5,11), 7:range(3,13), 8:range(3,13), 9:list(range(5,11))+[12], 10:[10,11]}
SOUPS={ # dark, mid, light, chunks
 "beef_stew":((112,48,24),(156,74,36),(196,110,56),[(232,128,40),(150,40,40),(236,206,140)]),
 "chicken_soup":((190,142,54),(224,184,88),(246,220,138),[(232,128,40),(250,236,200),(118,158,58)]),
 "burnt_stew":((24,20,18),(42,34,30),(66,52,44),[(16,12,10),(96,64,40)]),
}
for name,(d,m,l,chunks) in SOUPS.items():
    rnd=random.Random(name); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    for y,xs in AREA.items():
        for x in xs:
            c = l if y==6 or (y==7 and x<8) else m
            if y>=9 or x>=11: c=d
            if rnd.random()<0.25: c=rnd.choice(chunks)
            px[x,y]=c+(255,)
    if name=="burnt_stew":   # a little smoke above the burnt one
        for (x,y),a in {(7,4):150,(8,3):110,(7,2):70}.items(): px[x,y]=(90,90,96,a)
    img.save(f"{out}/{name}.png")
