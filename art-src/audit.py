# Checks the whole mod for missing names, models, textures, loot tables and broken recipe references.
import re, json, glob, os, sys
ROOT=sys.argv[1]; J=f"{ROOT}/src/main/java/net/skross/cozycookery"; A=f"{ROOT}/src/main/resources/assets/cozycookery"; D=f"{ROOT}/src/main/resources/data"
src=open(f"{J}/item/ModItems.java").read(); bsrc=open(f"{J}/block/ModBlocks.java").read()
items=set(re.findall(r'(?:food|bowlFood|registerSimpleItem|ITEMS\.register)\("([a-z0-9_]+)"',src))
blocks_with_item=set(re.findall(r'register(?:Block|Meal)\("([a-z0-9_]+)"',bsrc))
blocks_no_item=set(re.findall(r'BLOCKS\.register\("([a-z0-9_]+)"',bsrc))
# loops (chopping boards, stoves, ovens)
woods=re.search(r'WOOD_TYPES = List\.of\(([^)]*)\)',bsrc).group(1); woods=re.findall(r'"([a-z_]+)"',woods)
stoves=re.findall(r'"([a-z_]+)"',re.search(r'STOVE_TYPES = List\.of\(([^)]*)\)',bsrc).group(1))
blocks_with_item|={w+"_chopping_board" for w in woods}|{s+"_stove" for s in stoves}|{s+"_oven" for s in stoves}
lang=json.load(open(f"{A}/lang/en_us.json"))
problems=[]
for i in items:
    if f"item.cozycookery.{i}" not in lang: problems.append(f"missing name: item {i}")
    if not os.path.exists(f"{A}/models/item/{i}.json"): problems.append(f"missing item model: {i}")
for b in blocks_with_item|blocks_no_item:
    if f"block.cozycookery.{b}" not in lang: problems.append(f"missing name: block {b}")
    if not os.path.exists(f"{A}/blockstates/{b}.json"): problems.append(f"missing blockstate: {b}")
    if not os.path.exists(f"{D}/cozycookery/loot_table/blocks/{b}.json"): problems.append(f"missing loot table: {b}")
for b in blocks_with_item:
    if not os.path.exists(f"{A}/models/item/{b}.json"): problems.append(f"missing item model: block item {b}")
# every model's textures exist
for p in glob.glob(f"{A}/models/**/*.json",recursive=True):
    m=json.load(open(p))
    for k,t in m.get("textures",{}).items():
        if t.startswith("#") or not t.startswith("cozycookery:"): continue
        if not os.path.exists(f"{A}/textures/{t.split(':')[1]}.png"): problems.append(f"missing texture {t} in {os.path.basename(p)}")
    par=m.get("parent","")
    if par.startswith("cozycookery:") and not os.path.exists(f"{A}/models/{par.split(':')[1]}.json"): problems.append(f"missing parent {par} in {os.path.basename(p)}")
# every blockstate model exists
for p in glob.glob(f"{A}/blockstates/*.json"):
    for v in json.load(open(p))["variants"].values():
        mod=v["model"]
        if mod.startswith("cozycookery:") and not os.path.exists(f"{A}/models/{mod.split(':')[1]}.json"): problems.append(f"missing model {mod} in blockstate {os.path.basename(p)}")
# every cozycookery id used in recipes / loot / tags exists
known={f"cozycookery:{x}" for x in items|blocks_with_item}
def walk(o, path):
    if isinstance(o,dict):
        for k,v in o.items():
            if k in ("item","id","name") and isinstance(v,str) and v.startswith("cozycookery:") and v not in known and "/" not in v:
                problems.append(f"unknown id {v} in {path}")
            walk(v,path)
    elif isinstance(o,list):
        for v in o:
            if isinstance(v,str) and v.startswith("cozycookery:") and v not in known and "/" not in v and "tags/item" in path:
                problems.append(f"unknown id {v} in {path}")
            walk(v,path)
for p in glob.glob(f"{D}/**/*.json",recursive=True):
    if "/worldgen/" in p or "/neoforge/biome_modifier" in p or "tags/block" in p: continue
    walk(json.load(open(p)),p.replace(D,""))
# shaped crafting recipes that use the exact same pattern + keys (conflicts)
seen={}
for p in glob.glob(f"{D}/cozycookery/recipe/**/*.json",recursive=True):
    r=json.load(open(p))
    if r.get("type")=="minecraft:crafting_shaped":
        sig=(tuple(r["pattern"]),json.dumps(r["key"],sort_keys=True))
        if sig in seen: problems.append(f"same crafting recipe: {os.path.basename(p)} and {seen[sig]}")
        seen[sig]=os.path.basename(p)
# unused textures
used=set()
for p in glob.glob(f"{A}/models/**/*.json",recursive=True):
    for t in json.load(open(p)).get("textures",{}).values():
        if t.startswith("cozycookery:"): used.add(t.split(":")[1])
for p in glob.glob(f"{A}/textures/**/*.png",recursive=True):
    rel=p.split("/textures/")[1][:-4]
    if rel not in used: problems.append(f"unused texture: {rel}")
print(f"{len(items)} items, {len(blocks_with_item)} blocks with item, {len(blocks_no_item)} blocks without item")
print("\n".join(sorted(set(problems))) or "NO PROBLEMS")
