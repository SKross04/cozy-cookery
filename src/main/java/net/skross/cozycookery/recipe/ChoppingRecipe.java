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

import java.util.List;

/**
 * A chopping board recipe, loaded from JSON files in data/cozycookery/recipe/chopping/
 *
 * ingredient: what has to lie on the board
 * tool:       what you have to click with (e.g. #c:tools/knife)
 * results:    what you get. 1 result with count 1 stays on the board, anything else pops out.
 */
public record ChoppingRecipe(Ingredient ingredient, Ingredient tool, List<ItemStack> results)
        implements Recipe<ChoppingRecipeInput> {

    @Override
    public boolean matches(ChoppingRecipeInput input, Level level) {
        return ingredient.test(input.getItem(0));
    }

    public boolean matchesTool(ItemStack heldItem) {
        return tool.test(heldItem);
    }

    // A "single result" stays on the board so you can keep chopping (beef -> strips -> cubes -> ground)
    public boolean hasSingleResult() {
        return results.size() == 1 && results.get(0).getCount() == 1;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(ingredient);
        return list;
    }

    @Override
    public ItemStack assemble(ChoppingRecipeInput input, HolderLookup.Provider registries) {
        return results.get(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return results.get(0);
    }

    // "Special" keeps it out of the vanilla recipe book (we have no GUI anyway)
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CHOPPING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CHOPPING_TYPE.get();
    }

    // Turns the JSON file into a ChoppingRecipe (CODEC) and sends it to the client (STREAM_CODEC)
    public static class Serializer implements RecipeSerializer<ChoppingRecipe> {
        public static final MapCodec<ChoppingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ChoppingRecipe::ingredient),
                Ingredient.CODEC_NONEMPTY.fieldOf("tool").forGetter(ChoppingRecipe::tool),
                ItemStack.CODEC.listOf().fieldOf("results").forGetter(ChoppingRecipe::results)
        ).apply(inst, ChoppingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        Ingredient.CONTENTS_STREAM_CODEC, ChoppingRecipe::ingredient,
                        Ingredient.CONTENTS_STREAM_CODEC, ChoppingRecipe::tool,
                        ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()), ChoppingRecipe::results,
                        ChoppingRecipe::new);

        @Override
        public MapCodec<ChoppingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ChoppingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
