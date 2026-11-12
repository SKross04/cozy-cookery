package net.skross.cozycookery.block.custom;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

import java.util.function.Supplier;

/**
 * One class for all our crops. Vanilla's CropBlock already does everything:
 * AGE 0-7, growing with random ticks, bone meal, needing farmland and light.
 * We only have to tell it which item is its seed (used for the "pick block" key and for bone meal checks).
 */
public class ModCropBlock extends CropBlock {
    private final Supplier<? extends ItemLike> seed;

    public ModCropBlock(Properties properties, Supplier<? extends ItemLike> seed) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seed.get();
    }
}
