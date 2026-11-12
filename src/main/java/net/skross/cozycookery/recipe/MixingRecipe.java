package net.skross.cozycookery.recipe;

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
import net.neoforged.neoforge.common.util.RecipeMatcher;

import java.util.List;

/**
 * A mixing bowl recipe, loaded from JSON files in data/cozycookery/recipe/mixing/
 * ingredients: what has to be in the bowl (shapeless), max 8
 * result:      the dough / batter you get after one stir with the spoon
 */
public record MixingRecipe(List<Ingredient> ingredients, ItemStack result) implements Recipe<MixingRecipeInput> {

    @Override
    public boolean matches(MixingRecipeInput input, Level level) {
        if (input.size() != ingredients.size()) {
            return false;
        }
        return RecipeMatcher.findMatches(input.items(), ingredients) != null;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    @Override
    public ItemStack assemble(MixingRecipeInput input, HolderLookup.Provider registries) {
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
        return ModRecipes.MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.MIXING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<MixingRecipe> {
        public static final MapCodec<MixingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(MixingRecipe::ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(MixingRecipe::result)
        ).apply(inst, MixingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), MixingRecipe::ingredients,
                        ItemStack.STREAM_CODEC, MixingRecipe::result,
                        MixingRecipe::new);

        @Override
        public MapCodec<MixingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
