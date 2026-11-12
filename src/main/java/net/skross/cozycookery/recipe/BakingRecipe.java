package net.skross.cozycookery.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * An oven recipe, loaded from JSON files in data/cozycookery/recipe/baking/
 * ingredient:   what goes on the tray
 * cooking_time: ticks it needs (left in 50% longer than that -> Charred Scraps)
 * result:       what comes out (count can be more than 1, e.g. 8 cookies)
 */
public record BakingRecipe(Ingredient ingredient, int cookingTime, ItemStack result) implements Recipe<SingleRecipeInput> {

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return ingredient.test(input.item());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.BAKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.BAKING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<BakingRecipe> {
        public static final MapCodec<BakingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(BakingRecipe::ingredient),
                Codec.INT.optionalFieldOf("cooking_time", 400).forGetter(BakingRecipe::cookingTime),
                ItemStack.CODEC.fieldOf("result").forGetter(BakingRecipe::result)
        ).apply(inst, BakingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BakingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, BakingRecipe::ingredient,
                        ByteBufCodecs.VAR_INT, BakingRecipe::cookingTime,
                        ItemStack.STREAM_CODEC, BakingRecipe::result,
                        BakingRecipe::new);

        @Override
        public MapCodec<BakingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BakingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
