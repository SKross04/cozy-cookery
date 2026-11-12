import sys, math, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
from PIL import Image
out=sys.argv[1]
CER=[(150,140,128),(216,208,194),(238,232,220),(250,248,242)]
BAND=(196,112,70)    # terracotta band on the rim
def save(img,name): img.save(f"{out}/{name}.png")
# ---------- plate block texture (glazed ceramic) ----------
img=Image.new("RGBA",(16,16)); px=img.load(); rnd=random.Random(1)
for y in range(16):
    for x in range(16):
        d=max(abs(x-7.5),abs(y-7.5))
        c=CER[2]
        if d>=6.5: c=CER[3]
        if 5.5<=d<6.5: c=BAND
        if rnd.random()<0.05: c=CER[3]
        px[x,y]=c+(255,)
save(img,"block/plate")
# ---------- item icons ----------
def plate_icon(name, raw=False, food=None):
    col={}; fill={}
    rim=CER if not raw else [(110,104,110),(150,146,154),(170,166,176),(186,182,192)]
    for y in range(16):
        for x in range(16):
            d=math.hypot((x-7.5)/7.0,(y-8)/5.0)
            if d<=1:
                fill[(x,y)]=1
                c=rim[2] if d<0.72 else rim[3]
                if not raw and 0.72<=d<0.82: c=BAND
                if d>0.92 or y>11: c=rim[1]
                col[(x,y)]=c
    if food: food(col)
    img=new(); put(img,col); outline(img,fill,lambda p:rim[0]); save(img,"item/"+name)
plate_icon("plate"); plate_icon("raw_plate", raw=True)
# ---------- Full Breakfast ----------
BACON=[(150,52,40),(214,112,80),(246,224,196)]
def breakfast_food(col, ox=0, oy=0, sx=1.0, sy=1.0, skip=lambda x,y: False):
    def P(x,y,c):
        X=int(round(ox+x*sx)); Y=int(round(oy+y*sy))
        if not skip(X,Y): col[(X,Y)]=c
    for i in range(2):                                       # 2 bacon strips
        for t in range(5):
            P(4+t, 5+i*2, BACON[(t+i)%3 if t%2 else 0])
    for y in range(9,12):                                    # fried egg
        for x in range(4,8):
            P(x,y,(250,248,240))
    P(5,10,(252,196,40)); P(6,10,(252,196,40))
    for y in range(9,12):                                    # toast
        for x in range(9,12):
            P(x,y,(214,160,82) if (x,y)!=(9,9) else (132,72,24))
    P(10,5,(132,84,44)); P(11,6,(178,128,70)); P(10,6,(214,170,104))   # mushrooms
def breakfast_icon(col): breakfast_food(col, ox=0.5, oy=0.5, sx=1.0, sy=0.85)
plate_icon("full_breakfast", food=breakfast_icon)
# top texture for the block model: food drawn inside x/z 4..12 (the food area of the plate model)
img=Image.new("RGBA",(16,16),(0,0,0,0)); col={}
for y in range(4,12):
    for x in range(4,12):
        col[(x,y)]=(222,206,180)                             # base layer (bread crumbs / plate shadow)
breakfast_food(col, ox=0, oy=0)
put(img,col); save(img,"block/full_breakfast_top")
# side of the food layer
img=Image.new("RGBA",(16,16)); px=img.load()
for y in range(16):
    for x in range(16):
        px[x,y]=((214,160,82) if (x//2)%2 else (150,52,40))+(255,)
save(img,"block/full_breakfast_side")

# ---- v2: clearer Full Breakfast icon ----
def breakfast_icon(col):
    for i,y in enumerate((4,6)):                        # bacon strips, wavy
        for x in range(3,9):
            yy=y+((x+i)%2)
            col[(x,yy)]=BACON[0] if x in (3,8) else (BACON[2] if (x+i)%3==0 else BACON[1])
    for y in range(8,12):                               # fried egg
        for x in range(3,8):
            if not ((x in (3,7)) and (y in (8,11))): col[(x,y)]=(250,248,240)
    for p in [(5,9),(5,10),(6,9),(6,10)]: col[p]=(252,196,40)
    col[(5,9)]=(255,228,120)
    for y in range(7,11):                               # toast
        for x in range(9,13):
            col[(x,y)]=(132,72,24) if x in (9,12) or y in (7,10) else (214,160,82)
    for p,c in [((10,4),(96,58,30)),((11,4),(96,58,30)),((10,5),(214,170,104)),((11,5),(178,128,70))]: col[p]=c   # mushroom
plate_icon("full_breakfast", food=breakfast_icon)
