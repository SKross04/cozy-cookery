package net.skross.cozycookery.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.item.custom.KnifeItem;
import net.skross.cozycookery.item.custom.SpoonItem;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CozyCookery.MOD_ID);

    // Knife stats: 1 attack damage (+ the tier's bonus) and a fast attack speed of -2.0
    public static final DeferredItem<Item> FLINT_KNIFE = ITEMS.register("flint_knife",
            () -> new KnifeItem(ModToolTiers.FLINT, knifeProperties(ModToolTiers.FLINT)));
    public static final DeferredItem<Item> IRON_KNIFE = ITEMS.register("iron_knife",
            () -> new KnifeItem(Tiers.IRON, knifeProperties(Tiers.IRON)));
    public static final DeferredItem<Item> GOLDEN_KNIFE = ITEMS.register("golden_knife",
            () -> new KnifeItem(Tiers.GOLD, knifeProperties(Tiers.GOLD)));
    public static final DeferredItem<Item> DIAMOND_KNIFE = ITEMS.register("diamond_knife",
            () -> new KnifeItem(Tiers.DIAMOND, knifeProperties(Tiers.DIAMOND)));
    public static final DeferredItem<Item> NETHERITE_KNIFE = ITEMS.register("netherite_knife",
            () -> new KnifeItem(Tiers.NETHERITE, knifeProperties(Tiers.NETHERITE).fireResistant()));

    // Wooden spoon: 128 uses. Items with durability automatically stack to 1.
    public static final DeferredItem<Item> WOODEN_SPOON = ITEMS.register("wooden_spoon",
            () -> new SpoonItem(new Item.Properties().durability(128)));

    // ---------- Meat cuts (from the chopping board) ----------
    public static final DeferredItem<Item> RAW_BEEF_STRIPS = food("raw_beef_strips", ModFoodProperties.RAW_BEEF_CUT);
    public static final DeferredItem<Item> RAW_BEEF_CUBES = food("raw_beef_cubes", ModFoodProperties.RAW_BEEF_CUT);
    public static final DeferredItem<Item> GROUND_BEEF = food("ground_beef", ModFoodProperties.RAW_BEEF_CUT);

    public static final DeferredItem<Item> RAW_PORK_STRIPS = food("raw_pork_strips", ModFoodProperties.RAW_PORK_CUT);
    public static final DeferredItem<Item> RAW_PORK_CUBES = food("raw_pork_cubes", ModFoodProperties.RAW_PORK_CUT);
    public static final DeferredItem<Item> GROUND_PORK = food("ground_pork", ModFoodProperties.RAW_PORK_CUT);

    public static final DeferredItem<Item> RAW_MUTTON_STRIPS = food("raw_mutton_strips", ModFoodProperties.RAW_MUTTON_CUT);
    public static final DeferredItem<Item> RAW_MUTTON_CUBES = food("raw_mutton_cubes", ModFoodProperties.RAW_MUTTON_CUT);
    public static final DeferredItem<Item> GROUND_MUTTON = food("ground_mutton", ModFoodProperties.RAW_MUTTON_CUT);

    public static final DeferredItem<Item> RAW_CHICKEN_DRUMSTICK = food("raw_chicken_drumstick", ModFoodProperties.RAW_CHICKEN_PIECE);
    public static final DeferredItem<Item> RAW_CHICKEN_WING = food("raw_chicken_wing", ModFoodProperties.RAW_CHICKEN_PIECE);
    public static final DeferredItem<Item> COOKED_CHICKEN_DRUMSTICK = food("cooked_chicken_drumstick", ModFoodProperties.COOKED_CHICKEN_DRUMSTICK);
    public static final DeferredItem<Item> COOKED_CHICKEN_WING = food("cooked_chicken_wing", ModFoodProperties.COOKED_CHICKEN_WING);

    // ---------- Pot dishes: served in bowls, stack to 4 ----------
    public static final DeferredItem<Item> BEEF_STEW = bowlFood("beef_stew", ModFoodProperties.BEEF_STEW);
    public static final DeferredItem<Item> CHICKEN_SOUP = bowlFood("chicken_soup", ModFoodProperties.CHICKEN_SOUP);
    public static final DeferredItem<Item> BURNT_STEW = bowlFood("burnt_stew", ModFoodProperties.BURNT_STEW);

    // The empty bowl is given back by ModEvents.onFinishEating (tag cozycookery:bowl_foods)
    private static DeferredItem<Item> bowlFood(String name, FoodProperties foodProperties) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().stacksTo(4).food(foodProperties)));
    }

    private static DeferredItem<Item> food(String name, FoodProperties foodProperties) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().food(foodProperties)));
    }

    private static Item.Properties knifeProperties(Tier tier) {
        return new Item.Properties().attributes(SwordItem.createAttributes(tier, 1, -2.0f));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
