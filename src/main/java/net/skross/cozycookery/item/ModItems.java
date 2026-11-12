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
import net.skross.cozycookery.item.custom.SpatulaItem;
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

    // Wooden spatula: flips food in the frying pan. 128 uses.
    public static final DeferredItem<Item> WOODEN_SPATULA = ITEMS.register("wooden_spatula",
            () -> new SpatulaItem(new Item.Properties().durability(128)));

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

    // ---------- Chopped vegetables & fruit (from the chopping board) ----------
    public static final DeferredItem<Item> CARROT_SLICES = food("carrot_slices", ModFoodProperties.CARROT_CUT);
    public static final DeferredItem<Item> CARROT_CUBES = food("carrot_cubes", ModFoodProperties.CARROT_CUT);
    public static final DeferredItem<Item> POTATO_SLICES = food("potato_slices", ModFoodProperties.POTATO_CUT);
    public static final DeferredItem<Item> POTATO_CUBES = food("potato_cubes", ModFoodProperties.POTATO_CUT);
    public static final DeferredItem<Item> BEETROOT_CUBES = food("beetroot_cubes", ModFoodProperties.BEETROOT_CUT);
    public static final DeferredItem<Item> APPLE_SLICES = food("apple_slices", ModFoodProperties.APPLE_SLICES);
    public static final DeferredItem<Item> PUMPKIN_SLICE = food("pumpkin_slice", ModFoodProperties.PUMPKIN_SLICE);
    // Mushrooms aren't edible raw in vanilla, so sliced mushrooms aren't either
    public static final DeferredItem<Item> SLICED_MUSHROOMS = ITEMS.registerSimpleItem("sliced_mushrooms");

    // ---------- Fried in the frying pan ----------
    public static final DeferredItem<Item> SEARED_BEEF_STRIPS = food("seared_beef_strips", ModFoodProperties.SEARED_BEEF);
    public static final DeferredItem<Item> SEARED_BEEF_CUBES = food("seared_beef_cubes", ModFoodProperties.SEARED_BEEF);
    public static final DeferredItem<Item> BROWNED_GROUND_BEEF = food("browned_ground_beef", ModFoodProperties.SEARED_BEEF);
    public static final DeferredItem<Item> BACON = food("bacon", ModFoodProperties.FRIED_PORK);
    public static final DeferredItem<Item> FRIED_PORK_CUBES = food("fried_pork_cubes", ModFoodProperties.FRIED_PORK);
    public static final DeferredItem<Item> BROWNED_GROUND_PORK = food("browned_ground_pork", ModFoodProperties.FRIED_PORK);
    public static final DeferredItem<Item> SEARED_MUTTON_STRIPS = food("seared_mutton_strips", ModFoodProperties.SEARED_MUTTON);
    public static final DeferredItem<Item> SEARED_MUTTON_CUBES = food("seared_mutton_cubes", ModFoodProperties.SEARED_MUTTON);
    public static final DeferredItem<Item> BROWNED_GROUND_MUTTON = food("browned_ground_mutton", ModFoodProperties.SEARED_MUTTON);
    public static final DeferredItem<Item> POTATO_CHIPS = food("potato_chips", ModFoodProperties.FRIED_POTATO);
    public static final DeferredItem<Item> FRIED_POTATO_CUBES = food("fried_potato_cubes", ModFoodProperties.FRIED_POTATO);
    public static final DeferredItem<Item> FRIED_MUSHROOMS = food("fried_mushrooms", ModFoodProperties.FRIED_MUSHROOMS);
    public static final DeferredItem<Item> FRIED_EGG = food("fried_egg", ModFoodProperties.FRIED_EGG);
    public static final DeferredItem<Item> CHARRED_SCRAPS = food("charred_scraps", ModFoodProperties.CHARRED_SCRAPS);

    // ---------- Pot dishes: served in bowls, stack to 4 ----------
    public static final DeferredItem<Item> BEEF_STEW = bowlFood("beef_stew", ModFoodProperties.BEEF_STEW);
    public static final DeferredItem<Item> CHICKEN_SOUP = bowlFood("chicken_soup", ModFoodProperties.CHICKEN_SOUP);
    public static final DeferredItem<Item> VEGETABLE_SOUP = bowlFood("vegetable_soup", ModFoodProperties.VEGETABLE_SOUP);
    public static final DeferredItem<Item> PUMPKIN_SOUP = bowlFood("pumpkin_soup", ModFoodProperties.PUMPKIN_SOUP);
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
