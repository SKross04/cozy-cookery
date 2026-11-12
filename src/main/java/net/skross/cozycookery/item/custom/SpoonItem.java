package net.skross.cozycookery.item.custom;

import net.minecraft.world.item.Item;

public class SpoonItem extends Item {
    // For now the spoon is just a tool with durability.
    // Later the cooking pot will check "instanceof SpoonItem" and wear it down with every stir.
    public SpoonItem(Properties properties) {
        super(properties);
    }
}
