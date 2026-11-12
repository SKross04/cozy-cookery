# Cozy Cookery – art style (v4, approved)

Vanilla item technique + Let's Do charm. Scripts: `art-src/v4/` (`vanilla_style.py` = hand-pixelled sprites,
`bowls.py` = every bowl dish incl. the vanilla stew overrides). Older experiments in v2/v3 are not used.

- Hand-pixelled 16x16 grids, ~8-10 colours per item, like the current vanilla apple/carrot/potato.
- Saturated base colour, a small bright highlight cluster on the top-left, darker shade + darkest outline on the bottom-right.
- Outline = the object's own dark colours (warm dark top-left, darkest bottom-right), never pure black.
- A little pixel texture (two neighbouring shades alternating) in big areas; no smooth gradients, no noise.
- Produce gets Let's Do charm: leaf crowns, sprouts, roots.
- Dishes are seen from above: round wooden bowl (light rim) or white plate (grey-blue outline) filling the slot,
  food heaped inside as 3x2 pieces (light / highlight / dark + a shadow row), herbs as bright green specks.
- Tools reuse vanilla's ramps by brightness rank (knife tiers = sword tiers, handle = stick colours).
