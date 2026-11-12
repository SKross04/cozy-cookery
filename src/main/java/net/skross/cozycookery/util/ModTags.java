package net.skross.cozycookery.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
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

    public static class Items {
        // Foods served in a bowl: stack to 4 and give the bowl back (data/cozycookery/tags/item/bowl_foods.json)
        public static final TagKey<Item> BOWL_FOODS = createTag("bowl_foods");

        private static TagKey<Item> createTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(CozyCookery.MOD_ID, name));
        }
    }
}
