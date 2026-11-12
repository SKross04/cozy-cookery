package net.skross.cozycookery.item.custom;

import net.minecraft.world.item.Item;

// The mortar checks "instanceof PestleItem" to grind what lies in it. Each grind costs 1 durability.
public class PestleItem extends Item {
    public PestleItem(Properties properties) {
        super(properties);
    }
}
