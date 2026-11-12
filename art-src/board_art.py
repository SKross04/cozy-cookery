import sys, random
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
out=sys.argv[1]
TONES=[(198,144,86),(212,160,100),(184,130,76)]
GROOVE=(142,94,52); BEV_L=(228,186,128); BEV_D=(150,102,58); SIDE_T=(176,122,68); SIDE_B=(128,86,46); OUTL=(86,52,26)
def body_set():
    s=set()
    for x in range(2,12):
        for z in range(3,13): s.add((x,z))
    for z in range(4,12): s.add((1,z)); s.add((12,z))
    for z in range(6,10): s.add((13,z))
    for x in (14,15): s.add((x,6)); s.add((x,9))
    for z in (7,8): s.add((15,z))
    return s
def board_texture():
    rnd=random.Random(11); B=body_set(); col={}
    for (x,z) in B:
        c=TONES[(x//2)%3]
        if rnd.random()<0.12: c=tuple(int(v*0.9) for v in c)
        col[(x,z)]=c
    for x in range(3,11):                     # juice groove
        for z in range(4,12):
            if x in (3,10) or z in (4,11): col[(x,z)]=GROOVE
    for (x,z) in B:                            # bevel on the outer edge
        if (x-1,z) not in B or (x,z-1) not in B: col[(x,z)]=BEV_L
        elif (x+1,z) not in B or (x,z+1) not in B: col[(x,z)]=BEV_D
    for x in range(16):                        # side strip used by all side faces
        col[(x,14)]=SIDE_T; col[(x,15)]=SIDE_B
        if rnd.random()<0.15: col[(x,15)]=tuple(int(v*0.88) for v in SIDE_B)
    img=new(); put(img,col); img.save(f"{out}/chopping_board_block.png")
def board_icon():
    rnd=random.Random(5); fill={}; col={}
    top=set()
    for x in range(2,12):
        for y in range(4,11): top.add((x,y))
    for y in range(5,10): top.add((1,y)); top.add((12,y))
    for y in range(6,9): top.add((13,y))
    for x in (14,15): top.add((x,6)); top.add((x,8))
    top.add((15,7))
    for p in top:
        x,y=p; c=TONES[(x//2)%3]
        if rnd.random()<0.12: c=tuple(int(v*0.9) for v in c)
        col[p]=c; fill[p]=1
    for x in range(3,11):
        for y in range(5,10):
            if x in (3,10) or y in (5,9): col[(x,y)]=GROOVE
    for p in top:
        x,y=p
        if (x-1,y) not in top or (x,y-1) not in top: col[p]=BEV_L
    # thickness (front side of the board)
    for (x,y) in list(top):
        if (x,y+1) not in top and (x,y+1)!=(14,7) and (x,y+1)!=(14,8):
            for dy in (1,2):
                q=(x,y+dy)
                if q not in top: col[q]=SIDE_T if dy==1 else SIDE_B; fill[q]=1
    for q in [(14,7),(14,8)]: col.pop(q,None); fill.pop(q,None)   # hanging hole
    img=new(); put(img,col); outline(img,fill,lambda p:OUTL)
    px=img.load(); px[14,7]=(0,0,0,0)
    img.save(f"{out}/chopping_board.png")
board_texture(); board_icon()
