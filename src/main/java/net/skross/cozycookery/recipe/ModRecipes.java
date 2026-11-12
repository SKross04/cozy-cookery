package net.skross.cozycookery.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CozyCookery.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, CozyCookery.MOD_ID);

    // "type": "cozycookery:chopping" in the JSON files points to these
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ChoppingRecipe>> CHOPPING_SERIALIZER =
            SERIALIZERS.register("chopping", ChoppingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<ChoppingRecipe>> CHOPPING_TYPE =
            TYPES.register("chopping", () -> new RecipeType<ChoppingRecipe>() {
                @Override
                public String toString() {
                    return "chopping";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CookingRecipe>> COOKING_SERIALIZER =
            SERIALIZERS.register("cooking", CookingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<CookingRecipe>> COOKING_TYPE =
            TYPES.register("cooking", () -> new RecipeType<CookingRecipe>() {
                @Override
                public String toString() {
                    return "cooking";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FryingRecipe>> FRYING_SERIALIZER =
            SERIALIZERS.register("frying", FryingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<FryingRecipe>> FRYING_TYPE =
            TYPES.register("frying", () -> new RecipeType<FryingRecipe>() {
                @Override
                public String toString() {
                    return "frying";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            SERIALIZERS.register("grinding", GrindingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<GrindingRecipe>> GRINDING_TYPE =
            TYPES.register("grinding", () -> new RecipeType<GrindingRecipe>() {
                @Override
                public String toString() {
                    return "grinding";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MixingRecipe>> MIXING_SERIALIZER =
            SERIALIZERS.register("mixing", MixingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<MixingRecipe>> MIXING_TYPE =
            TYPES.register("mixing", () -> new RecipeType<MixingRecipe>() {
                @Override
                public String toString() {
                    return "mixing";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BakingRecipe>> BAKING_SERIALIZER =
            SERIALIZERS.register("baking", BakingRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<BakingRecipe>> BAKING_TYPE =
            TYPES.register("baking", () -> new RecipeType<BakingRecipe>() {
                @Override
                public String toString() {
                    return "baking";
                }
            });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
