package net.skross.cozycookery.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.ModBlocks;
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

    // ---------- Baking ingredients ----------
    public static final DeferredItem<Item> FLOUR = ITEMS.registerSimpleItem("flour");
    // Raw doughs and batters from the mixing bowl (not edible, they go in the oven)
    public static final DeferredItem<Item> BREAD_DOUGH = ITEMS.registerSimpleItem("bread_dough");
    public static final DeferredItem<Item> COOKIE_DOUGH = ITEMS.registerSimpleItem("cookie_dough");
    public static final DeferredItem<Item> CAKE_BATTER = ITEMS.registerSimpleItem("cake_batter");
    public static final DeferredItem<Item> RAW_PUMPKIN_PIE = ITEMS.registerSimpleItem("raw_pumpkin_pie");
    public static final DeferredItem<Item> RAW_APPLE_PIE = ITEMS.registerSimpleItem("raw_apple_pie");
    public static final DeferredItem<Item> APPLE_PIE = food("apple_pie", ModFoodProperties.APPLE_PIE);

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

    // ---------- Crops ----------
    public static final DeferredItem<Item> TOMATO = food("tomato", ModFoodProperties.TOMATO);
    // Tomato seeds are planted on a trellis (see TrellisBlock), not straight on farmland
    public static final DeferredItem<Item> TOMATO_SEEDS = ITEMS.registerSimpleItem("tomato_seeds");

    // ItemNameBlockItem: an item that places a block (the crop) but keeps its own name
    public static final DeferredItem<Item> CABBAGE = food("cabbage", ModFoodProperties.CABBAGE);
    public static final DeferredItem<Item> CABBAGE_SEEDS = ITEMS.register("cabbage_seeds",
            () -> new ItemNameBlockItem(ModBlocks.CABBAGE_CROP.get(), new Item.Properties()));
    // The onion is planted itself, like vanilla carrots and potatoes
    public static final DeferredItem<Item> ONION = ITEMS.register("onion",
            () -> new ItemNameBlockItem(ModBlocks.ONION_CROP.get(), new Item.Properties().food(ModFoodProperties.ONION)));
    // Garlic: the bulb is food, you plant a single clove (1 bulb -> 4 cloves in the crafting table)
    public static final DeferredItem<Item> GARLIC = food("garlic", ModFoodProperties.GARLIC);
    public static final DeferredItem<Item> GARLIC_CLOVE = ITEMS.register("garlic_clove",
            () -> new ItemNameBlockItem(ModBlocks.GARLIC_CROP.get(), new Item.Properties()));

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

    // ---------- Prepped new crops (chopping board) ----------
    public static final DeferredItem<Item> TOMATO_SLICES = food("tomato_slices", ModFoodProperties.TOMATO);
    public static final DeferredItem<Item> DICED_TOMATO = food("diced_tomato", ModFoodProperties.TOMATO);
    public static final DeferredItem<Item> ONION_RINGS = food("onion_rings", ModFoodProperties.ONION);
    public static final DeferredItem<Item> DICED_ONION = food("diced_onion", ModFoodProperties.ONION);
    public static final DeferredItem<Item> MINCED_GARLIC = ITEMS.registerSimpleItem("minced_garlic");
    public static final DeferredItem<Item> CABBAGE_LEAVES = food("cabbage_leaves", ModFoodProperties.CABBAGE_LEAF);
    public static final DeferredItem<Item> SHREDDED_CABBAGE = food("shredded_cabbage", ModFoodProperties.CABBAGE_LEAF);
    public static final DeferredItem<Item> BREAD_SLICE = food("bread_slice", ModFoodProperties.BREAD_SLICE);
    public static final DeferredItem<Item> TOAST = food("toast", ModFoodProperties.TOAST);
    public static final DeferredItem<Item> FRIED_ONIONS = food("fried_onions", ModFoodProperties.FRIED_ONIONS);
    public static final DeferredItem<Item> RAW_GARLIC_BREAD = ITEMS.registerSimpleItem("raw_garlic_bread");
    public static final DeferredItem<Item> GARLIC_BREAD = food("garlic_bread", ModFoodProperties.GARLIC_BREAD);

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
    public static final DeferredItem<Item> TOMATO_SOUP = bowlFood("tomato_soup", ModFoodProperties.TOMATO_SOUP);
    public static final DeferredItem<Item> ONION_SOUP = bowlFood("onion_soup", ModFoodProperties.ONION_SOUP);
    public static final DeferredItem<Item> CABBAGE_SOUP = bowlFood("cabbage_soup", ModFoodProperties.CABBAGE_SOUP);
    public static final DeferredItem<Item> GARDEN_SALAD = bowlFood("garden_salad", ModFoodProperties.GARDEN_SALAD);
    public static final DeferredItem<Item> COLESLAW = bowlFood("coleslaw", ModFoodProperties.COLESLAW);
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
