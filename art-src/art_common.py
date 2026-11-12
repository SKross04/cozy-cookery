from PIL import Image
def new(): return Image.new("RGBA",(16,16),(0,0,0,0))
def put(img, pts):
    px = img.load()
    for (x,y),c in pts.items():
        if 0<=x<16 and 0<=y<16: px[x,y] = tuple(c)+(255,) if len(c)==3 else c
def outline(img, fill, color_of):
    """fill: dict (x,y)->part name ; color_of(part)->outline colour"""
    px = img.load()
    for (x,y),part in list(fill.items()):
        for dx,dy in ((1,0),(-1,0),(0,1),(0,-1)):
            nx,ny = x+dx,y+dy
            if 0<=nx<16 and 0<=ny<16 and (nx,ny) not in fill and px[nx,ny][3]==0:
                px[nx,ny] = color_of(part)+(255,)
def preview(paths, out, scale=8, cols=8):
    ims=[Image.open(p) for p in paths]
    w=16*scale+8; rows=(len(ims)+cols-1)//cols
    c=Image.new("RGBA",(cols*w, rows*w),(52,48,46,255))
    for i,im in enumerate(ims):
        c.paste(im.resize((16*scale,16*scale),Image.NEAREST),((i%cols)*w+4,(i//cols)*w+4),im.resize((16*scale,16*scale),Image.NEAREST))
    c.save(out)
