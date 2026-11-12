# Tiny isometric preview of Minecraft block models (flat-shaded faces, average texture colour)
import json, math, sys
from PIL import Image, ImageDraw
def avg(path):
    im=Image.open(path).convert("RGBA").crop((0,0,16,16)); px=[p for p in im.getdata() if p[3]>0]
    return tuple(sum(c[i] for c in px)//len(px) for i in range(3))
def rot(p, r):
    if not r: return p
    a=math.radians(r["angle"]); o=r["origin"]; x,y,z=p[0]-o[0],p[1]-o[1],p[2]-o[2]
    if r["axis"]=="x": y,z=y*math.cos(a)-z*math.sin(a), y*math.sin(a)+z*math.cos(a)
    if r["axis"]=="y": x,z=x*math.cos(a)+z*math.sin(a), -x*math.sin(a)+z*math.cos(a)
    if r["axis"]=="z": x,y=x*math.cos(a)-y*math.sin(a), x*math.sin(a)+y*math.cos(a)
    return (x+o[0],y+o[1],z+o[2])
def render(model_path, texmap, size=220):
    m=json.load(open(model_path)); img=Image.new("RGB",(size,size),(52,48,46)); d=ImageDraw.Draw(img)
    def proj(p):
        x,y,z=p; s=size/30
        return (size/2+(x-z)*s*0.87, size*0.72-(y*1.0)*s-(x+z)*s*0.5+8*s*0.5)
    polys=[]
    for e in m["elements"]:
        (x1,y1,z1),(x2,y2,z2)=e["from"],e["to"]
        F={"up":[(x1,y2,z1),(x2,y2,z1),(x2,y2,z2),(x1,y2,z2)],"south":[(x1,y1,z2),(x2,y1,z2),(x2,y2,z2),(x1,y2,z2)],
           "east":[(x2,y1,z1),(x2,y1,z2),(x2,y2,z2),(x2,y2,z1)],"north":[(x1,y1,z1),(x2,y1,z1),(x2,y2,z1),(x1,y2,z1)],
           "west":[(x1,y1,z1),(x1,y1,z2),(x1,y2,z2),(x1,y2,z1)]}
        shade={"up":1.0,"south":0.8,"east":0.65,"north":0.8,"west":0.65}
        for k,pts in F.items():
            if k not in e["faces"]: continue
            tex=e["faces"][k]["texture"].lstrip("#"); col=texmap.get(tex,(200,0,200))
            pts=[rot(p,e.get("rotation")) for p in pts]
            depth=sum(p[0]+p[2]+p[1]*0.5 for p in pts)/4
            polys.append((depth,[proj(p) for p in pts],tuple(int(c*shade[k]) for c in col)))
    for _,pts,col in sorted(polys,key=lambda t:t[0]):
        d.polygon(pts,fill=col,outline=tuple(max(0,c-30) for c in col))
    return img
if __name__=="__main__":
    out=sys.argv[1]; models=sys.argv[2:]
    T="/home/claude/cozy-cookery/src/main/resources/assets/cozycookery/textures/block/"
    texmap={"head":avg(T+"cabbage_head.png"),"leaf":avg(T+"cabbage_leaf.png")}
    ims=[render(p,texmap) for p in models]
    c=Image.new("RGB",(220*len(ims),220)); [c.paste(im,(i*220,0)) for i,im in enumerate(ims)]; c.save(out)
