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
import net.neoforged.neoforge.common.util.RecipeMatcher;

import java.util.List;

/**
 * A cooking pot recipe, loaded from JSON files in data/cozycookery/recipe/cooking/
 *
 * ingredients:  what has to be in the pot (shapeless: the order doesn't matter), max 6
 * cooking_time: how long it simmers, in ticks (20 ticks = 1 second)
 * result:       the dish + how many servings (count). Each serving is scooped out with a bowl.
 */
public record CookingRecipe(List<Ingredient> ingredients, int cookingTime, ItemStack result)
        implements Recipe<CookingRecipeInput> {

    @Override
    public boolean matches(CookingRecipeInput input, Level level) {
        if (input.size() != ingredients.size()) {
            return false;
        }
        // RecipeMatcher finds out if every item matches a different ingredient, in any order
        return RecipeMatcher.findMatches(input.items(), ingredients) != null;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.addAll(ingredients);
        return list;
    }

    @Override
    public ItemStack assemble(CookingRecipeInput input, HolderLookup.Provider registries) {
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
        return ModRecipes.COOKING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.COOKING_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<CookingRecipe> {
        public static final MapCodec<CookingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").forGetter(CookingRecipe::ingredients),
                Codec.INT.optionalFieldOf("cooking_time", 600).forGetter(CookingRecipe::cookingTime),
                ItemStack.CODEC.fieldOf("result").forGetter(CookingRecipe::result)
        ).apply(inst, CookingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CookingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), CookingRecipe::ingredients,
                        ByteBufCodecs.VAR_INT, CookingRecipe::cookingTime,
                        ItemStack.STREAM_CODEC, CookingRecipe::result,
                        CookingRecipe::new);

        @Override
        public MapCodec<CookingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CookingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
