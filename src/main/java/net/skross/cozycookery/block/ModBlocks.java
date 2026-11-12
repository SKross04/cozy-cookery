package net.skross.cozycookery.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.custom.ChoppingBoardBlock;
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.block.custom.FryingPanBlock;
import net.skross.cozycookery.block.custom.MixingBowlBlock;
import net.skross.cozycookery.block.custom.MortarBlock;
import net.skross.cozycookery.block.custom.OvenBlock;
import net.skross.cozycookery.block.custom.StoveBlock;
import net.skross.cozycookery.item.ModItems;

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
