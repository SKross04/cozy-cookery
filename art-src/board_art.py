import sys, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
out=sys.argv[1]
WOODS = {  # base colour per wood type, all other shades are derived from it
 "oak":(198,146,88), "spruce":(130,92,56), "birch":(220,200,142), "jungle":(184,130,92),
 "acacia":(190,102,56), "dark_oak":(96,64,36), "mangrove":(136,56,50), "cherry":(228,174,164),
 "bamboo":(214,192,98), "crimson":(130,58,86), "warped":(56,132,122),
}
def sh(c,f): return tuple(max(0,min(255,int(v*f))) for v in c)
def palette(base):
    return dict(tones=[base,sh(base,1.07),sh(base,0.92)], groove=sh(base,0.72), bev_l=sh(base,1.16),
                bev_d=sh(base,0.76), side_t=sh(base,0.88), side_b=sh(base,0.64), outl=sh(base,0.42))
def body_set():
    s=set()
    for x in range(2,12):
        for z in range(3,13): s.add((x,z))
    for z in range(4,12): s.add((1,z)); s.add((12,z))
    for z in range(6,10): s.add((13,z))
    for x in (14,15): s.add((x,6)); s.add((x,9))
    for z in (7,8): s.add((15,z))
    return s
def board_texture(wood,P):
    rnd=random.Random(wood); B=body_set(); col={}
    for (x,z) in B:
        c=P["tones"][(x//2)%3]
        if rnd.random()<0.12: c=sh(c,0.9)
        col[(x,z)]=c
    for x in range(3,11):
        for z in range(4,12):
            if x in (3,10) or z in (4,11): col[(x,z)]=P["groove"]
    for (x,z) in B:
        if (x-1,z) not in B or (x,z-1) not in B: col[(x,z)]=P["bev_l"]
        elif (x+1,z) not in B or (x,z+1) not in B: col[(x,z)]=P["bev_d"]
    for x in range(16):
        col[(x,14)]=P["side_t"]; col[(x,15)]=P["side_b"]
        if rnd.random()<0.15: col[(x,15)]=sh(P["side_b"],0.88)
    img=new(); put(img,col); img.save(f"{out}/block/{wood}_chopping_board.png")
def board_icon(wood,P):
    rnd=random.Random(wood+"i"); fill={}; col={}; top=set()
    for x in range(2,12):
        for y in range(4,11): top.add((x,y))
    for y in range(5,10): top.add((1,y)); top.add((12,y))
    for y in range(6,9): top.add((13,y))
    for x in (14,15): top.add((x,6)); top.add((x,8))
    top.add((15,7))
    for p in top:
        x,y=p; c=P["tones"][(x//2)%3]
        if rnd.random()<0.12: c=sh(c,0.9)
        col[p]=c; fill[p]=1
    for x in range(3,11):
        for y in range(5,10):
            if x in (3,10) or y in (5,9): col[(x,y)]=P["groove"]
    for p in top:
        x,y=p
        if (x-1,y) not in top or (x,y-1) not in top: col[p]=P["bev_l"]
    for (x,y) in list(top):
        if (x,y+1) not in top and (x,y+1) not in ((14,7),(14,8)):
            for dy in (1,2):
                q=(x,y+dy)
                if q not in top: col[q]=P["side_t"] if dy==1 else P["side_b"]; fill[q]=1
    for q in [(14,7),(14,8)]: col.pop(q,None); fill.pop(q,None)
    img=new(); put(img,col); outline(img,fill,lambda p:P["outl"])
    img.load()[14,7]=(0,0,0,0)
    img.save(f"{out}/item/{wood}_chopping_board.png")
for w,b in WOODS.items():
    P=palette(b); board_texture(w,P); board_icon(w,P)
