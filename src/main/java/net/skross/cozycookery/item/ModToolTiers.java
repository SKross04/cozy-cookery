package net.skross.cozycookery.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public class ModToolTiers {
    // Flint is an early-game material, roughly as strong as stone.
    // (incorrect blocks, durability, mining speed, attack damage bonus, enchantability, repair item)
    public static final Tier FLINT = new SimpleTier(BlockTags.INCORRECT_FOR_STONE_TOOL,
            131, 4.0f, 1.0f, 5, () -> Ingredient.of(Items.FLINT));
}
