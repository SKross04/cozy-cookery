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
import net.minecraft.world.level.Level;

/**
 * A frying pan recipe, loaded from JSON files in data/cozycookery/recipe/frying/
 *
 * ingredient:   the raw piece
 * cooking_time: ticks it needs in total (it has to be flipped halfway)
 * result:       the fried piece
 */
public record FryingRecipe(Ingredient ingredient, int cookingTime, ItemStack result)
        implements Recipe<FryingRecipeInput> {

    @Override
    public boolean matches(FryingRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ItemStack assemble(FryingRecipeInput input, HolderLookup.Provider registries) {
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
        return ModRecipes.FRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.FRYING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<FryingRecipe> {
        public static final MapCodec<FryingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(FryingRecipe::ingredient),
                Codec.INT.optionalFieldOf("cooking_time", 200).forGetter(FryingRecipe::cookingTime),
                ItemStack.CODEC.fieldOf("result").forGetter(FryingRecipe::result)
        ).apply(inst, FryingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FryingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, FryingRecipe::ingredient,
                        ByteBufCodecs.VAR_INT, FryingRecipe::cookingTime,
                        ItemStack.STREAM_CODEC, FryingRecipe::result,
                        FryingRecipe::new);

        @Override
        public MapCodec<FryingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FryingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
