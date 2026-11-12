package net.skross.cozycookery.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.skross.cozycookery.CozyCookery;

public class ModTags {
    public static class Blocks {
        // Blocks that can heat a cooking pot placed on top of them (data/cozycookery/tags/block/heat_sources.json)
        public static final TagKey<Block> HEAT_SOURCES = createTag("heat_sources");

        private static TagKey<Block> createTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, name));
        }
    }
}
