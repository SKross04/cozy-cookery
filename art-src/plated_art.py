import sys, random, math, json
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
T=sys.argv[1]                      # textures folder
CER=[(150,140,128),(216,208,194),(238,232,220),(250,248,242)]; BAND=(196,112,70)
def load(name): return Image.open(f"{T}/item/{name}.png").convert("RGBA")
def shape(fn):                     # tiny painter for vanilla foods (drawn ourselves, not copied)
    img=Image.new("RGBA",(16,16),(0,0,0,0)); fn(img.load()); return img
def cod(px):
    for y in range(5,11):
        for x in range(3,14): px[x,y]=(236,226,200,255) if (x+y)%4 else (214,180,120,255)
def salmon(px):
    for y in range(5,11):
        for x in range(3,14): px[x,y]=(232,128,88,255) if (x+y)%3 else (246,196,170,255)
def baked_potato(px):
    for y in range(16):
        for x in range(16):
            if ((x-7.5)/6)**2+((y-8)/4.5)**2<=1: px[x,y]=(150,96,48,255) if ((x-7.5)/6)**2+((y-8)/4.5)**2>0.5 else (246,220,140,255)
def berries(px):
    for (cx,cy) in [(5,6),(10,6),(7,10),(11,11),(4,11)]:
        for dx in (0,1):
            for dy in (0,1): px[cx+dx,cy+dy]=(200,30,40,255)
        px[cx,cy]=(240,110,110,255)
def honey(px):
    for y in range(4,13):
        for x in range(3,13): px[x,y]=(232,164,40,255) if (x+y)%3 else (250,206,90,255)
VANILLA={"minecraft:cooked_cod":shape(cod),"minecraft:cooked_salmon":shape(salmon),
         "minecraft:baked_potato":shape(baked_potato),"minecraft:sweet_berries":shape(berries),"minecraft:honey_bottle":shape(honey)}
def sprite(name):
    if name in VANILLA: return VANILLA[name]
    return load(name.split(":")[-1])
def shrink(img, size):
    bbox=img.getbbox(); img=img.crop(bbox) if bbox else img
    small=img.resize((size,size),Image.LANCZOS); px=small.load()
    for y in range(size):
        for x in range(size):
            r,g,b,a=px[x,y]; px[x,y]=(r,g,b,255) if a>90 else (0,0,0,0)
    return small
def avg_colour(img):
    px=[p for p in img.getdata() if p[3]>0]
    return tuple(sum(c[i] for c in px)//max(1,len(px)) for i in range(3))
def plate_icon(food_painter):
    img=new(); col={}; fill={}
    for y in range(16):
        for x in range(16):
            d=math.hypot((x-7.5)/7.0,(y-8)/5.0)
            if d<=1:
                fill[(x,y)]=1; c=CER[2] if d<0.72 else CER[3]
                if 0.72<=d<0.82: c=BAND
                if d>0.92 or y>11: c=CER[1]
                col[(x,y)]=c
    put(img,col); outline(img,fill,lambda p:CER[0]); food_painter(img); return img
def component_meal(name, components):
    comps=[sprite(c) for c in components][:4]
    while len(comps)<4: comps.append(comps[len(comps)%max(1,len(comps))])
    # block top: one component per quarter of the 8x8 food area (x/z 4..12)
    top=Image.new("RGBA",(16,16),(0,0,0,0)); base=Image.new("RGBA",(8,8),(222,206,180,255)); top.paste(base,(4,4))
    for q,(qx,qy) in enumerate([(4,4),(8,4),(4,8),(8,8)]):
        s=shrink(comps[q],4); top.alpha_composite(s,(qx,qy))
    top.save(f"{T}/block/{name}_top.png")
    side=Image.new("RGBA",(16,16)); sp=side.load(); cols=[avg_colour(c) for c in comps]
    for y in range(16):
        for x in range(16): sp[x,y]=cols[(x//4)%4]+(255,)
    side.save(f"{T}/block/{name}_side.png")
    def food(img):
        for q,(qx,qy) in enumerate([(3,4),(8,4),(3,8),(8,8)]):
            img.alpha_composite(shrink(comps[q],5),(qx,qy))
    plate_icon(food).save(f"{T}/item/{name}.png")
def hexc(h): h=h.lstrip("#"); return tuple(int(h[i:i+2],16) for i in (0,2,4))
def bowl_dish(name, colour, palette_from=None, chunks=()):
    rnd=random.Random(name); c=hexc(colour)
    pal=[c, tuple(min(255,int(v*1.18)) for v in c), tuple(int(v*0.82) for v in c)]
    if palette_from:
        try:
            src=load(palette_from); pal+= [p[:3] for p in src.getdata() if p[3]>0][::3]
        except FileNotFoundError: pass
    pal+=list(chunks)
    top=Image.new("RGBA",(16,16),(0,0,0,0)); tp=top.load()
    for y in range(4,12):
        for x in range(4,12): tp[x,y]=rnd.choice(pal)+(255,)
    top.save(f"{T}/block/{name}_top.png")
    side=Image.new("RGBA",(16,16)); sp=side.load()
    for y in range(16):
        for x in range(16): sp[x,y]=(pal[2] if y%3 else pal[0])+(255,)
    side.save(f"{T}/block/{name}_side.png")
    def food(img):
        p=img.load()
        for y in range(16):
            for x in range(16):
                if math.hypot((x-7.5)/5.0,(y-7.6)/3.2)<=1: p[x,y]=rnd.choice(pal)+(255,)
    plate_icon(food).save(f"{T}/item/{name}.png")
if __name__=="__main__":
    spec=json.load(open(sys.argv[2]))
    for m in spec["component_meals"]: component_meal(m["name"], m["components"])
    for d in spec["bowl_dishes"]: bowl_dish(d["name"], d["colour"], d.get("palette_from"), [tuple(c) for c in d.get("chunks",[])])
