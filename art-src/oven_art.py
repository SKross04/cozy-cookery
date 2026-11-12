import sys, math
sys.path.insert(0,"/home/claude/scripts")
from art_common import *
out=sys.argv[1]
# Apple pie: golden lattice crust in a copper pie tin
def apple_pie():
    col={}; fill={}
    CU=[(110,56,32),(164,88,50),(204,124,72),(234,164,108)]
    CRUST=[(176,110,44),(214,150,70),(236,184,104)]
    for y in range(16):
        for x in range(16):
            top=((x-7.5)/6.8)**2+((y-7.5)/3.2)**2<=1
            side=7.5<=y<=11.5 and abs(x-7.5)<=6.8-(y-7.5)*0.35
            if side: fill[(x,y)]=1; col[(x,y)]=CU[2] if x<9 else CU[1]
            if top:
                fill[(x,y)]=1
                rim=((x-7.5)/6.8)**2+((y-7.5)/3.2)**2>0.6
                if rim: col[(x,y)]=CRUST[1] if y<8 else CRUST[0]
                else:
                    col[(x,y)]=(200,150,70)                         # baked apple filling
                    if x%3==0 or (x+y)%5==0: col[(x,y)]=CRUST[2]     # lattice
    img=new(); put(img,col); outline(img,fill,lambda p:CU[0]); img.save(f"{out}/item/apple_pie.png")
apple_pie()
