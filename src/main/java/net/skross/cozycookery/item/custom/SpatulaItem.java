package net.skross.cozycookery.item.custom;

import net.minecraft.world.item.Item;

// The frying pan checks "instanceof SpatulaItem" to flip the food. Each flip costs 1 durability.
public class SpatulaItem extends Item {
    public SpatulaItem(Properties properties) {
        super(properties);
    }
}
