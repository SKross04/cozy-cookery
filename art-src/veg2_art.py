import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
def save(img,name): img.save(f"{out}/{name}.png")
def coins(name, o, rim, flesh, core, seeds=None, ring_hole=False):
    col={}
    for (cx,cy) in [(4,4.5),(11,4),(4.5,11),(11.5,11)]:
        for y in range(16):
            for x in range(16):
                if math.hypot(x-cx,y-cy)<=3.7: col[(x,y)]=o
        for y in range(16):
            for x in range(16):
                dd=math.hypot(x-cx,y-cy)
                if dd<=2.7:
                    col[(x,y)] = rim if dd>1.8 else (core if dd<0.9 else flesh)
                    if ring_hole and dd<1.2: col.pop((x,y),None)
        if seeds:
            for dx,dy in [(-1,0),(1,0),(0,1)]: col[(int(cx)+dx,int(cy)+dy)]=seeds
    img=new(); put(img,col); save(img,"item/"+name)
def cubes(name,o,d,m,l,fleck=None):
    col={}
    for cx,cy in [(5,3),(9,5),(2,7),(7,9),(11,10)]:
        for dx in range(-1,5):
            for dy in range(-1,5): col[(cx+dx,cy+dy)]=o
        for dx in range(4):
            for dy in range(4):
                c = l if dy==0 else (d if dx==3 or dy==3 else m)
                if (dx,dy)==(1,1) and fleck: c=fleck
                col[(cx+dx,cy+dy)]=c
    img=new(); put(img,col); save(img,"item/"+name)
def pile(name, colors, o, size=(5.4,3.6)):
    rnd=random.Random(name); col={}; fill={}
    for y in range(16):
        for x in range(16):
            if ((x-7.5)/size[0])**2+((y-9.5)/size[1])**2<=1:
                fill[(x,y)]=1; col[(x,y)]=rnd.choice(colors)
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
def leaf(name, light, mid, dark, o, count=1):
    col={}; fill={}
    for k in range(count):
        ox,oy=(k*3,k*2)
        for y in range(16):
            for x in range(16):
                u=(x-ox-7.5); v=(y-oy-7.5)
                if (u/6.0)**2+(v/4.2)**2<=1 and 0<=x<16 and 0<=y<16:
                    fill[(x,y)]=1; col[(x,y)]=mid if (x+y)%4 else light
                    if abs(v-u*0.25)<0.6: col[(x,y)]=light         # middle vein
                    if v>2.5: col[(x,y)]=dark
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
def shreds(name, colors, o):
    rnd=random.Random(name); col={}; fill={}
    for i in range(9):
        x0=rnd.randint(1,10); y0=rnd.randint(3,12); c=rnd.choice(colors)
        for t in range(rnd.randint(3,5)):
            p=(x0+t, y0+((t+i)%2)); col[p]=c; fill[p]=1
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
def bread_slice(name, crust, crumb, o, specks=None, toast=False):
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            top=((x-7.5)/5.5)**2+((y-6)/3.6)**2<=1 and y<=6
            body=2<=x<=13 and 6<=y<=13
            if top or body:
                fill[(x,y)]=1
                edge=(x in (2,13)) or y==13 or (top and ((x-7.5)/5.5)**2+((y-6)/3.6)**2>0.62)
                col[(x,y)]=crust if edge else crumb
                if not edge and toast and (x+y)%3==0: col[(x,y)]=tuple(int(c*0.92) for c in crumb)
    if specks:
        rnd=random.Random(name)
        for _ in range(7): col[(rnd.randint(4,11),rnd.randint(5,11))]=specks
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
def curls(name, colors, o):
    rnd=random.Random(name); col={}; fill={}
    for (cx,cy) in [(5,6),(10,5),(7,10),(11,11),(4,11)]:
        for a in range(0,300,30):
            r=2.2; x=int(round(cx+r*math.cos(math.radians(a)))); y=int(round(cy+r*math.sin(math.radians(a))))
            col[(x,y)]=rnd.choice(colors); fill[(x,y)]=1
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
# ---- tomato ----
coins("tomato_slices",(120,20,22),(206,40,38),(236,92,72),(250,150,120),seeds=(246,214,120))
cubes("diced_tomato",(120,20,22),(176,30,32),(214,52,46),(240,110,90),(246,214,120))
# ---- onion ----
coins("onion_rings",(150,120,150),(232,214,232),(248,240,248),(248,240,248),ring_hole=True)
cubes("diced_onion",(150,130,150),(214,200,214),(236,226,236),(250,246,250))
# ---- garlic ----
pile("minced_garlic",[(240,234,224),(250,248,240),(220,210,196)],(150,134,120),size=(4.6,3.0))
# ---- cabbage ----
leaf("cabbage_leaves",(178,214,136),(128,186,92),(86,148,62),(48,96,40),count=2)
shreds("shredded_cabbage",[(128,186,92),(178,214,136),(150,200,110)],(48,96,40))
# ---- bread ----
bread_slice("bread_slice",(170,104,40),(238,214,160),(110,64,24))
bread_slice("toast",(132,72,24),(214,160,82),(86,46,16),toast=True)
bread_slice("raw_garlic_bread",(170,104,40),(244,226,170),(110,64,24),specks=(96,150,60))
bread_slice("garlic_bread",(132,72,24),(226,176,90),(86,46,16),specks=(80,128,46),toast=True)
# ---- pan ----
curls("fried_onions",[(196,128,52),(226,168,84),(170,100,36)],(110,58,20))
# ---- bowl layers (on top of the vanilla bowl) ----
AREA = {6:range(5,11), 7:range(3,13), 8:range(3,13), 9:list(range(5,11))+[12], 10:[10,11]}
def bowl_layer(name, d, m, l, chunks, density=0.25, up=()):
    rnd=random.Random(name); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    for y,xs in AREA.items():
        for x in xs:
            c = l if y==6 or (y==7 and x<8) else m
            if y>=9 or x>=11: c=d
            if rnd.random()<density: c=rnd.choice(chunks)
            px[x,y]=c+(255,)
    for (x,y),c in up: px[x,y]=c+(255,)            # things sticking out of the bowl
    save(img,"item/"+name)
bowl_layer("tomato_soup",(150,30,24),(196,56,40),(224,96,70),[(240,140,110),(108,150,52)])
bowl_layer("onion_soup",(140,86,36),(176,120,54),(206,158,84),[(232,200,140),(214,160,82)],up=[((7,5),(214,160,82)),((8,5),(214,160,82)),((9,5),(170,104,40))])
bowl_layer("cabbage_soup",(112,140,60),(150,180,84),(184,206,116),[(236,132,36),(178,214,136),(236,214,140)])
bowl_layer("garden_salad",(62,120,48),(96,160,66),(140,196,96),[(206,40,38),(236,132,36),(232,214,232),(178,214,136)],density=0.4,
           up=[((5,5),(128,186,92)),((6,4),(140,196,96)),((9,5),(96,160,66)),((10,4),(206,40,38))])
bowl_layer("coleslaw",(206,200,170),(232,228,204),(246,244,228),[(236,132,36),(150,200,110),(178,214,136)],density=0.35)
