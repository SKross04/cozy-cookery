import sys, random, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
out=sys.argv[1]
MEAT = {  # outline, dark, mid, light, fat, fatshade
 "beef":   ((96,22,30),(150,34,44),(188,52,58),(220,92,88),(246,226,208),(220,188,168)),
 "pork":   ((132,60,70),(204,102,112),(230,140,146),(248,180,180),(252,236,228),(236,206,198)),
 "mutton": ((84,24,34),(136,40,54),(168,58,70),(200,90,96),(242,228,212),(218,196,178)),
}
KRAFT=((118,88,54),(186,152,106),(212,182,136),(232,208,166))
def strips(n):
    o,d,m,l,f,fs=MEAT[n]; fill={}; col={}
    for k,base in enumerate((12,16,20)):           # three wavy ribbons along the diagonal
        for y in range(16):
            for x in range(16):
                u=x-y; v=x+y
                if not (-9+k<=u<=9+k): continue
                c=base+round(0.8*math.sin(u*0.7+k))
                if v in (c,c+1):
                    fill[(x,y)]=k
                    col[(x,y)] = l if v==c else m
                    if (u+k*3)%7==0: col[(x,y)] = f             # marbling
                    if abs(u-(-9+k))<=0 or abs(u-(9+k))<=0: col[(x,y)] = d
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/raw_{n}_strips.png")
def cubes(n):
    o,d,m,l,f,fs=MEAT[n]; fill={}; col={}
    def cube(cx,cy):
        # 4x4 cube: top row light, right column dark, a fat fleck
        for dx in range(4):
            for dy in range(4):
                p=(cx+dx,cy+dy); fill[p]=1
                c = l if dy==0 else (d if dx==3 else m)
                if (dx,dy)==(1,2): c=f
                col[p]=c
    for cx,cy in [(2,9),(7,10),(10,6),(5,5),(1,4)]: cube(cx,cy)
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/raw_{n}_cubes.png")
def ground(n):
    o,d,m,l,f,fs=MEAT[n]; rnd=random.Random(n); fill={}; col={}
    for y in range(16):              # butcher paper
        for x in range(16):
            if 1<=x<=14 and 9<=y<=14 and not ((x in (1,14)) and (y in (9,14))):
                fill[(x,y)]="p"; col[(x,y)] = KRAFT[3] if y==9 else (KRAFT[1] if y==14 else KRAFT[2])
                if x==7 and y>9: col[(x,y)] = KRAFT[1]   # fold
    for y in range(16):              # the mound of ground meat on top
        for x in range(16):
            r=((x-7.5)/5.6)**2+((y-8.2)/4.2)**2
            if r<=1 and y<=11:
                fill[(x,y)]="m"; rr=rnd.random()
                c = l if (y<=6 and rr<0.5) else (d if rr<0.2 else m)
                if rnd.random()<0.16: c=f
                col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p: KRAFT[0] if p=="p" else o); img.save(f"{out}/ground_{n}.png")
BONE=((122,106,86),(206,192,168),(238,228,206),(252,249,238))
CHICK={"raw":((156,100,86),(220,160,140),(240,194,172),(252,224,206),(212,146,128)),
       "cooked":((98,46,16),(160,82,28),(204,122,46),(236,170,82),(128,60,20))}
def drum(kind):
    o,d,m,l,s=CHICK[kind]; rnd=random.Random(kind+"d"); fill={}; col={}
    for i in range(5):                                   # bone
        for t in (0,1):
            p=(9+i+t,6-i); fill[p]="b"; col[p]=BONE[2] if t==0 else BONE[1]
    for p in [(13,1),(14,1),(14,2),(15,2),(13,0)]: fill[p]="b"; col[p]=BONE[3]
    for y in range(16):                                  # meat
        for x in range(16):
            r=((x-6)/4.6)**2+((y-10)/4.0)**2
            r2=((x-8.8)/2.2)**2+((y-6.8)/2.2)**2
            if r<=1 or r2<=1:
                fill[(x,y)]="m"
                c = l if (x+y)<13 else (d if (x+y)>19 else m)
                if rnd.random()<0.12: c=s
                col[(x,y)]=c
    img=new(); put(img,col); outline(img,fill,lambda p: BONE[0] if p=="b" else o); img.save(f"{out}/{kind}_chicken_drumstick.png")
def wing(kind):
    o,d,m,l,s=CHICK[kind]; rnd=random.Random(kind+"w"); fill={}; col={}
    for y in range(16):
        for x in range(16):
            a=((x-5.5)/3.6)**2+((y-10.5)/3.0)**2<=1                 # drumette
            b=abs((x-y)-(-1))<=1.2 and 5<=x<=11 and 4<=y<=11        # middle segment
            c=((x-11.8)/2.4)**2+((y-4.2)/2.8)**2<=1                 # tip
            if a or b or c:
                fill[(x,y)]=1
                cc = l if (x+y)<12 or (c and y<=3) else (d if (x+y)>19 else m)
                if rnd.random()<0.12: cc=s
                col[(x,y)]=cc
    for p in [(8,8),(9,7)]: col[p]=d    # joint crease
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/{kind}_chicken_wing.png")
for n in MEAT: strips(n); cubes(n); ground(n)
for k in CHICK: drum(k); wing(k)

# ---- v2: clearer strips, separated cubes, hand-drawn wing ----
def strips(n):
    o,d,m,l,f,fs=MEAT[n]; fill={}; col={}
    for k,(base,ulo,uhi) in enumerate([(10,-6,7),(14,-8,9),(18,-7,6)]):
        for y in range(16):
            for x in range(16):
                u=x-y; v=x+y
                if not (ulo<=u<=uhi): continue
                c=base+(1 if (u//3)%2==0 else 0)       # gentle wave, same for all strips
                if v in (c,c+1):
                    fill[(x,y)]=k
                    col[(x,y)] = l if v==c else m
                    if (u+k*2)%5==0: col[(x,y)] = f    # fat marbling
                    if u in (ulo,uhi): col[(x,y)] = d
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/raw_{n}_strips.png")
def cubes(n):
    o,d,m,l,f,fs=MEAT[n]; col={}
    for cx,cy in [(5,3),(9,5),(2,7),(7,9),(11,10)]:     # back to front
        for dx in range(-1,5):
            for dy in range(-1,5):
                col[(cx+dx,cy+dy)]=o
        for dx in range(4):
            for dy in range(4):
                c = l if dy==0 else (d if dx==3 or dy==3 else m)
                if (dx,dy)==(1,1): c=f
                col[(cx+dx,cy+dy)]=c
    img=new(); put(img,col); img.save(f"{out}/raw_{n}_cubes.png")
WING=["................",
      "...........LL...",
      "..........LMML..",
      "..........LMMD..",
      ".........LMSMD..",
      "........LMMMD...",
      ".......LMMMMD...",
      "......LMMSMD....",
      "..LLLLMMMMD.....",
      ".LMMSMMMMMD.....",
      ".LMMMMMMMD......",
      ".MMMMMSDD.......",
      "..MDDDD.........",
      "................",
      "................",
      "................"]
def wing(kind):
    o,d,m,l,s=CHICK[kind]; fill={}; col={}
    for y,row in enumerate(WING):
        for x,ch in enumerate(row):
            if ch!='.': fill[(x,y)]=1; col[(x,y)]={'L':l,'M':m,'D':d,'S':s}[ch]
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/{kind}_chicken_wing.png")
for n in MEAT: strips(n); cubes(n)
for k in CHICK: wing(k)

# ---- v3: thicker strips, wing tip + joint crease ----
def strips(n):
    o,d,m,l,f,fs=MEAT[n]; fill={}; col={}
    for k,(base,ulo,uhi) in enumerate([(9,-5,6),(14,-7,8),(19,-6,5)]):
        for y in range(16):
            for x in range(16):
                u=x-y; v=x+y
                if not (ulo<=u<=uhi): continue
                if v in (base,base+1,base+2):
                    fill[(x,y)]=k
                    col[(x,y)] = l if v==base else (d if v==base+2 else m)
                    if v==base+1 and (u+k*3)%6==0: col[(x,y)] = f
                    if u in (ulo,uhi): col[(x,y)] = d
    img=new(); put(img,col); outline(img,fill,lambda p:o); img.save(f"{out}/raw_{n}_strips.png")
WING[0]="............L..."
WING[1]="..........LLML.."
WING[6]=".......LMDMMD..."
WING[7]="......LMDMSMD..."
for n in MEAT: strips(n)
for k in CHICK: wing(k)

# ---- v4: V-shaped wing (drumette + flat) ----
WING[:]=["................",
         "................",
         "...........L....",
         "..........LML...",
         ".........LMMD...",
         "........LMMMD...",
         "..LL...LMMMD....",
         ".LMML.LMMSMD....",
         ".LMMMLDMMMMD....",
         ".LMSMMDMMMD.....",
         "..LMMMDMMMD.....",
         "...DMMMMMD......",
         "....DDMSD.......",
         "......DD........",
         "................",
         "................"]
for k in CHICK: wing(k)
