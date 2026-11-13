package net.skross.cozycookery.block;

import net.skross.cozycookery.block.custom.CookbookBlock;
import net.skross.cozycookery.block.custom.ToolRackBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.custom.ChoppingBoardBlock;
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.block.custom.FryingPanBlock;
import net.skross.cozycookery.block.custom.MixingBowlBlock;
import net.skross.cozycookery.block.custom.ModCropBlock;
import net.skross.cozycookery.block.custom.MortarBlock;
import net.skross.cozycookery.block.custom.OvenBlock;
import net.skross.cozycookery.block.custom.PlatedMealBlock;
import net.skross.cozycookery.block.custom.StoveBlock;
import net.skross.cozycookery.block.custom.TomatoTrellisBlock;
import net.skross.cozycookery.block.custom.TrellisBlock;
import net.skross.cozycookery.block.custom.WildCropBlock;
import net.skross.cozycookery.item.ModItems;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CozyCookery.MOD_ID);

    // ---------- Chopping boards: one per wood type, all using the same ChoppingBoardBlock class ----------
    public static final List<String> WOOD_TYPES = List.of(
            "oak", "spruce", "birch", "jungle", "acacia", "dark_oak",
            "mangrove", "cherry", "bamboo", "crimson", "warped");

    // Key = wood type ("oak"), value = the registered block ("oak_chopping_board")
    public static final Map<String, DeferredBlock<Block>> CHOPPING_BOARDS = registerChoppingBoards();

    private static Map<String, DeferredBlock<Block>> registerChoppingBoards() {
        Map<String, DeferredBlock<Block>> boards = new LinkedHashMap<>();
        for (String wood : WOOD_TYPES) {
            boolean netherWood = wood.equals("crimson") || wood.equals("warped"); // nether wood doesn't burn
            boards.put(wood, registerBlock(wood + "_chopping_board", () -> {
                BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WOOD)
                        .strength(0.5f)
                        .sound(wood.equals("bamboo") ? SoundType.BAMBOO_WOOD
                                : wood.equals("cherry") ? SoundType.CHERRY_WOOD
                                : netherWood ? SoundType.NETHER_WOOD : SoundType.WOOD)
                        .noOcclusion();
                if (!netherWood) {
                    properties.ignitedByLava();
                }
                return new ChoppingBoardBlock(properties);
            }));
        }
        return boards;
    }

    public static final DeferredBlock<Block> COOKING_POT = registerBlock("cooking_pot",
            () -> new CookingPotBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f, 6.0f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()));

    public static final DeferredBlock<Block> FRYING_PAN = registerBlock("frying_pan",
            () -> new FryingPanBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.LANTERN)
                    .noOcclusion()));

    public static final DeferredBlock<Block> MORTAR = registerBlock("mortar",
            () -> new MortarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(1.5f, 6.0f)
                    .sound(SoundType.STONE)
                    .noOcclusion()));

    public static final DeferredBlock<Block> MIXING_BOWL = registerBlock("mixing_bowl",
            () -> new MixingBowlBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(0.8f)
                    .sound(SoundType.WOOD)
                    .ignitedByLava()
                    .noOcclusion()));

    // ---------- Trellis + tomatoes (2 blocks tall) ----------
    // Wall rack for kitchen tools: nearby stations use the tools hanging on it
    public static final DeferredBlock<Block> TOOL_RACK = registerBlock("tool_rack",
            () -> new ToolRackBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()));

    // The cookbook lying open on a counter (its item is the Cookbook in ModItems)
    public static final DeferredBlock<Block> COOKBOOK = BLOCKS.register("cookbook",
            () -> new CookbookBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED)
                    .strength(0.5f)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<Block> TRELLIS = registerBlock("trellis",
            () -> new TrellisBlock(trellisProperties()));
    // No BlockItem: you get it by using tomato seeds on a trellis
    public static final DeferredBlock<Block> TOMATO_TRELLIS = BLOCKS.register("tomato_trellis",
            () -> new TomatoTrellisBlock(trellisProperties().sound(SoundType.CROP)));

    // ---------- Crops on farmland (no BlockItem: you plant them with the seed / the vegetable) ----------
    public static final DeferredBlock<Block> CABBAGE_CROP = BLOCKS.register("cabbage_crop",
            () -> new ModCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), ModItems.CABBAGE_SEEDS));
    public static final DeferredBlock<Block> ONION_CROP = BLOCKS.register("onion_crop",
            () -> new ModCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS), ModItems.ONION));
    public static final DeferredBlock<Block> GARLIC_CROP = BLOCKS.register("garlic_crop",
            () -> new ModCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARROTS), ModItems.GARLIC_CLOVE));

    public static final DeferredBlock<Block> WILD_CABBAGE = registerBlock("wild_cabbage",
            () -> new WildCropBlock(wildPlantProperties()));
    public static final DeferredBlock<Block> WILD_ONION = registerBlock("wild_onion",
            () -> new WildCropBlock(wildPlantProperties()));
    public static final DeferredBlock<Block> WILD_GARLIC = registerBlock("wild_garlic",
            () -> new WildCropBlock(wildPlantProperties()));

    private static BlockBehaviour.Properties trellisProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .noCollission()
                .strength(0.2f)
                .sound(SoundType.SCAFFOLDING)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    // ---------- Wild crops: spawn in the world, drop the vegetable + seeds ----------
    public static final DeferredBlock<Block> WILD_TOMATO = registerBlock("wild_tomato",
            () -> new WildCropBlock(wildPlantProperties()));

    private static BlockBehaviour.Properties wildPlantProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.SWEET_BERRY_BUSH)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    // ---------- Plated meals: placed and eaten bite by bite (4 bites) ----------
    // Value per meal = (sum of its components + about 25% bonus) / 4 bites
    // All meals, in order (used for the creative tab)
    public static final List<DeferredBlock<Block>> MEALS = new ArrayList<>();

    public static final DeferredBlock<Block> FULL_BREAKFAST = registerMeal("full_breakfast", 5, 0.6f);
    public static final DeferredBlock<Block> STEAK_AND_CHIPS = registerMeal("steak_and_chips", 7, 0.8f);
    public static final DeferredBlock<Block> CHICKEN_DINNER = registerMeal("chicken_dinner", 4, 0.6f);
    public static final DeferredBlock<Block> PANCAKE_STACK = registerMeal("pancake_stack", 4, 0.6f);
    public static final DeferredBlock<Block> BEEF_STIR_FRY = registerMeal("beef_stir_fry", 5, 0.6f);
    public static final DeferredBlock<Block> PORK_AND_CABBAGE = registerMeal("pork_and_cabbage", 4, 0.6f);
    public static final DeferredBlock<Block> MUTTON_DINNER = registerMeal("mutton_dinner", 6, 0.8f);
    public static final DeferredBlock<Block> FISH_AND_CHIPS = registerMeal("fish_and_chips", 4, 0.6f);
    public static final DeferredBlock<Block> SALMON_DINNER = registerMeal("salmon_dinner", 4, 0.6f);
    public static final DeferredBlock<Block> LOADED_POTATO = registerMeal("loaded_potato", 5, 0.6f);
    public static final DeferredBlock<Block> OMELETTE_PLATE = registerMeal("omelette_plate", 2, 0.6f);

    // Plate versions of the bowl dishes: bowl dish + plate in the crafting table (you get the bowl back)
    public static final DeferredBlock<Block> PLATED_BEEF_STEW = registerMeal("plated_beef_stew", 3, 0.6f);
    public static final DeferredBlock<Block> PLATED_CHICKEN_SOUP = registerMeal("plated_chicken_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_VEGETABLE_SOUP = registerMeal("plated_vegetable_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_PUMPKIN_SOUP = registerMeal("plated_pumpkin_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_TOMATO_SOUP = registerMeal("plated_tomato_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_ONION_SOUP = registerMeal("plated_onion_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_CABBAGE_SOUP = registerMeal("plated_cabbage_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_MUTTON_STEW = registerMeal("plated_mutton_stew", 3, 0.6f);
    public static final DeferredBlock<Block> PLATED_SPAGHETTI_POMODORO = registerMeal("plated_spaghetti_pomodoro", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_SPAGHETTI_BOLOGNESE = registerMeal("plated_spaghetti_bolognese", 3, 0.6f);
    public static final DeferredBlock<Block> PLATED_SPAGHETTI_AND_MEATBALLS = registerMeal("plated_spaghetti_and_meatballs", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_MUSHROOM_STEW = registerMeal("plated_mushroom_stew", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_BEETROOT_SOUP = registerMeal("plated_beetroot_soup", 2, 0.6f);
    public static final DeferredBlock<Block> PLATED_RABBIT_STEW = registerMeal("plated_rabbit_stew", 3, 0.6f);

    private static DeferredBlock<Block> registerMeal(String name, int nutritionPerBite, float saturationPerBite) {
        DeferredBlock<Block> meal = registerBlock(name, () -> new PlatedMealBlock(BlockBehaviour.Properties.of()
                .mapColor(MapColor.TERRACOTTA_WHITE)
                .strength(0.5f)
                .sound(SoundType.DECORATED_POT)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY), nutritionPerBite, saturationPerBite));
        MEALS.add(meal);
        return meal;
    }

    // ---------- Stoves: one per brick type, all using the same StoveBlock class ----------
    public static final List<String> STOVE_TYPES = List.of(
            "brick", "stone_brick", "deepslate_brick", "mud_brick", "tuff_brick");

    // Key = brick type ("stone_brick"), value = the registered block ("stone_brick_stove")
    public static final Map<String, DeferredBlock<Block>> STOVES = registerStoves();

    private static Map<String, DeferredBlock<Block>> registerStoves() {
        Map<String, DeferredBlock<Block>> stoves = new LinkedHashMap<>();
        for (String type : STOVE_TYPES) {
            SoundType sound = switch (type) {
                case "deepslate_brick" -> SoundType.DEEPSLATE_BRICKS;
                case "mud_brick" -> SoundType.MUD_BRICKS;
                case "tuff_brick" -> SoundType.TUFF_BRICKS;
                default -> SoundType.STONE;
            };
            stoves.put(type, registerBlock(type + "_stove", () -> new StoveBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(sound)
                    .lightLevel(state -> state.getValue(StoveBlock.LIT) ? 13 : 0)
                    .noOcclusion())));
        }
        return stoves;
    }

    // ---------- Ovens: same brick types as the stoves ----------
    // Key = brick type ("stone_brick"), value = the registered block ("stone_brick_oven")
    public static final Map<String, DeferredBlock<Block>> OVENS = registerOvens();

    private static Map<String, DeferredBlock<Block>> registerOvens() {
        Map<String, DeferredBlock<Block>> ovens = new LinkedHashMap<>();
        for (String type : STOVE_TYPES) {
            SoundType sound = switch (type) {
                case "deepslate_brick" -> SoundType.DEEPSLATE_BRICKS;
                case "mud_brick" -> SoundType.MUD_BRICKS;
                case "tuff_brick" -> SoundType.TUFF_BRICKS;
                default -> SoundType.STONE;
            };
            ovens.put(type, registerBlock(type + "_oven", () -> new OvenBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(2.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .sound(sound)
                    .lightLevel(state -> state.getValue(OvenBlock.LIT) ? 13 : 0)
                    .noOcclusion())));
        }
        return ovens;
    }

    // Registers the block AND a BlockItem with the same name, so you can hold and place it.
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
