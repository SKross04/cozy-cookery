# Cozy Cookery – art style

Original "cozy" style, inspired by the feel of Let's Do and Farmer's Delight, never a copy.

- 16x16 textures, item textures drawn diagonally (handle bottom-left, tip top-right) like vanilla tools.
- Warm, soft palettes; 3-4 shades per material; light comes from the top-left.
- Outlines are a dark shade of the material itself (dark steel, dark red, dark wood), never pure black.
- Small storytelling details: twine wraps, rivets, different woods per tool tier, butcher paper under ground meat.
- Wood: warm honey/oak tones; blocks get bevelled edges and butcher-block strips.
- Raw meat: marbling in fat colour; cooked food: golden-brown with darker crispy spots.
- Blocks: chunky but low-profile 3D models (rounded corners via stepped elements, real holes in handles).

The Python scripts in this folder generate the current textures (need Pillow): 
`python art_common.py` is shared helpers; the others take an output folder as argument.
