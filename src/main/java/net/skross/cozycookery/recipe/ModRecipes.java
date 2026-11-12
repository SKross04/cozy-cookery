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

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}
