import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
def save(img,name): img.save(f"{out}/{name}.png")
def blob(name, base, light, dark, o, extras=(), shape=None):
    shape=shape or (lambda x,y: ((x-7.5)/6.0)**2+((y-9)/4.6)**2<=1)
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            if shape(x,y):
                fill[(x,y)]=1; col[(x,y)] = light if (x+y)<13 else (dark if (x+y)>20 else base)
    for p,c in extras: col[p]=c; fill[p]=1
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
def noodles(name, colors, o, sauce=None):
    rnd=random.Random(name); col={}; fill={}
    for k in range(7):
        cx=7.5+rnd.uniform(-2,2); cy=9+rnd.uniform(-1.5,1.5); r=rnd.uniform(2.5,4.5); a0=rnd.uniform(0,6.28)
        for t in range(26):
            a=a0+t*0.24; x=int(round(cx+r*math.cos(a))); y=int(round(cy+r*0.7*math.sin(a)))
            if 0<=x<16 and 0<=y<16: col[(x,y)]=rnd.choice(colors); fill[(x,y)]=1
    if sauce:
        for _ in range(14):
            x=rnd.randint(5,10); y=rnd.randint(7,11); col[(x,y)]=rnd.choice(sauce); fill[(x,y)]=1
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
def balls(name, base, light, dark, o, n=5):
    col={}; fill={}
    for (cx,cy) in [(5,6),(10,6),(4,11),(9,11),(12,10)][:n]:
        for y in range(16):
            for x in range(16):
                d=math.hypot(x-cx,y-cy)
                if d<=2.6:
                    fill[(x,y)]=1; col[(x,y)]= light if (x-cx)+(y-cy)<-1 else (dark if (x-cx)+(y-cy)>1.5 else base)
    img=new(); put(img,col); outline(img,fill,lambda p:o); save(img,"item/"+name)
# pasta
blob("pasta_dough",(244,214,120),(252,232,160),(214,180,88),(150,118,50),shape=lambda x,y: math.hypot(x-7.5,y-9)<=5.6)
noodles("raw_noodles",[(246,222,140),(236,206,116),(250,236,170)],(176,140,60))
noodles("cooked_noodles",[(250,236,180),(244,224,160),(252,244,206)],(190,160,96))
# pancakes
blob("pancake_batter",(248,236,196),(252,246,222),(228,212,166),(176,156,110),shape=lambda x,y: ((x-7.5)/6.4)**2+((y-10)/3.6)**2<=1)
def pancake():
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            top=((x-7.5)/6.8)**2+((y-8)/3.4)**2<=1
            side=8<=y<=10 and abs(x-7.5)<=6.8
            if side: fill[(x,y)]=1; col[(x,y)]=(196,128,52)
            if top:
                fill[(x,y)]=1; col[(x,y)]=(226,166,82) if (x*3+y)%7 else (196,128,52)
                if ((x-7.5)/6.8)**2+((y-8)/3.4)**2>0.7: col[(x,y)]=(240,196,116)
    img=new(); put(img,col); outline(img,fill,lambda p:(120,72,24)); save(img,"item/pancake")
pancake()
# meatballs
balls("raw_meatballs",(220,130,130),(240,170,166),(186,100,104),(120,60,62))
balls("meatballs",(146,82,50),(182,112,70),(110,58,34),(70,36,20))
# omelette
blob("omelette_mix",(250,214,80),(255,234,140),(232,188,56),(170,128,30),
     extras=[((5,8),(206,40,38)),((9,9),(232,214,232)),((7,11),(206,40,38)),((10,11),(232,214,232))],
     shape=lambda x,y: ((x-7.5)/6.2)**2+((y-10)/3.8)**2<=1)
def omelette():
    col={}; fill={}
    for y in range(16):
        for x in range(16):
            if ((x-7.5)/6.6)**2+((y-9)/4.4)**2<=1 and y>=6:
                fill[(x,y)]=1; col[(x,y)]=(250,214,80) if (x+y)%5 else (226,176,60)
                if y<=7: col[(x,y)]=(255,232,140)
    for p in [(5,9),(9,10),(11,8)]: col[p]=(196,128,52)
    img=new(); put(img,col); outline(img,fill,lambda p:(170,120,30)); save(img,"item/omelette")
omelette()
# bowl layers (on top of the vanilla bowl)
AREA = {6:range(5,11), 7:range(3,13), 8:range(3,13), 9:list(range(5,11))+[12], 10:[10,11]}
def bowl_layer(name, d, m, l, chunks, density=0.25, up=()):
    rnd=random.Random(name); img=Image.new("RGBA",(16,16),(0,0,0,0)); px=img.load()
    for y,xs in AREA.items():
        for x in xs:
            c = l if y==6 or (y==7 and x<8) else m
            if y>=9 or x>=11: c=d
            if rnd.random()<density: c=rnd.choice(chunks)
            px[x,y]=c+(255,)
    for (x,y),c in up: px[x,y]=c+(255,)
    save(img,"item/"+name)
NOODLE=[(250,236,180),(244,224,160)]
bowl_layer("spaghetti_pomodoro",(170,40,30),(206,64,44),(232,110,80),NOODLE,density=0.45,up=[((6,5),NOODLE[0]),((8,5),NOODLE[1]),((9,4),(108,150,52))])
bowl_layer("spaghetti_bolognese",(120,46,28),(156,66,40),(186,96,60),NOODLE+[(110,58,34)],density=0.45,up=[((6,5),NOODLE[0]),((8,5),(110,58,34))])
bowl_layer("spaghetti_and_meatballs",(170,40,30),(206,64,44),(232,110,80),NOODLE,density=0.4,
           up=[((5,5),(146,82,50)),((6,5),(146,82,50)),((9,5),(146,82,50)),((10,5),(110,58,34)),((7,4),NOODLE[0])])
bowl_layer("mutton_stew",(104,62,36),(138,88,54),(170,118,76),[(236,132,36),(236,214,140),(160,96,70)])
bowl_layer("fruit_salad",(200,60,60),(236,120,96),(250,180,150),[(240,190,60),(206,40,38),(120,170,60),(250,240,200),(240,140,40)],density=0.6,
           up=[((6,5),(206,40,38)),((9,5),(240,190,60)),((7,4),(120,170,60))])
