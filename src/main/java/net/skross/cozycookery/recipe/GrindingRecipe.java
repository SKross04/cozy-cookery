package net.skross.cozycookery.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * A mortar & pestle recipe, loaded from JSON files in data/cozycookery/recipe/grinding/
 * ingredient: what lies in the mortar, result: what you get after one grind with the pestle.
 * (SingleRecipeInput is vanilla's ready-made "1 item" recipe input.)
 */
public record GrindingRecipe(Ingredient ingredient, ItemStack result) implements Recipe<SingleRecipeInput> {

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
        return ModRecipes.GRINDING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.GRINDING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<GrindingRecipe> {
        public static final MapCodec<GrindingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(GrindingRecipe::ingredient),
                ItemStack.CODEC.fieldOf("result").forGetter(GrindingRecipe::result)
        ).apply(inst, GrindingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, GrindingRecipe::ingredient,
                        ItemStack.STREAM_CODEC, GrindingRecipe::result,
                        GrindingRecipe::new);

        @Override
        public MapCodec<GrindingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GrindingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
