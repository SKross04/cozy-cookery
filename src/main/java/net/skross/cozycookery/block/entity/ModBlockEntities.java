package net.skross.cozycookery.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.ModBlocks;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CozyCookery.MOD_ID);

    // Links the ChoppingBoardBlockEntity class to all chopping board blocks
    public static final Supplier<BlockEntityType<ChoppingBoardBlockEntity>> CHOPPING_BOARD_BE =
            BLOCK_ENTITIES.register("chopping_board_be", () -> BlockEntityType.Builder.of(
                    ChoppingBoardBlockEntity::new,
                    // every wood variant uses the same block entity
                    ModBlocks.CHOPPING_BOARDS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)
            ).build(null));

    public static final Supplier<BlockEntityType<StoveBlockEntity>> STOVE_BE =
            BLOCK_ENTITIES.register("stove_be", () -> BlockEntityType.Builder.of(
                    StoveBlockEntity::new,
                    ModBlocks.STOVES.values().stream().map(DeferredBlock::get).toArray(Block[]::new)
            ).build(null));

    public static final Supplier<BlockEntityType<ToolRackBlockEntity>> TOOL_RACK_BE =
            BLOCK_ENTITIES.register("tool_rack_be", () -> BlockEntityType.Builder.of(
                    ToolRackBlockEntity::new, ModBlocks.TOOL_RACK.get()).build(null));
    public static final Supplier<BlockEntityType<PlateBlockEntity>> PLATE_BE =
            BLOCK_ENTITIES.register("plate_be", () -> BlockEntityType.Builder.of(
                    PlateBlockEntity::new, ModBlocks.PLATE.get()).build(null));
    public static final Supplier<BlockEntityType<CookingPotBlockEntity>> COOKING_POT_BE =
            BLOCK_ENTITIES.register("cooking_pot_be", () -> BlockEntityType.Builder.of(
                    CookingPotBlockEntity::new, ModBlocks.COOKING_POT.get()).build(null));

    public static final Supplier<BlockEntityType<FryingPanBlockEntity>> FRYING_PAN_BE =
            BLOCK_ENTITIES.register("frying_pan_be", () -> BlockEntityType.Builder.of(
                    FryingPanBlockEntity::new, ModBlocks.FRYING_PAN.get()).build(null));

    public static final Supplier<BlockEntityType<MortarBlockEntity>> MORTAR_BE =
            BLOCK_ENTITIES.register("mortar_be", () -> BlockEntityType.Builder.of(
                    MortarBlockEntity::new, ModBlocks.MORTAR.get()).build(null));

    public static final Supplier<BlockEntityType<MixingBowlBlockEntity>> MIXING_BOWL_BE =
            BLOCK_ENTITIES.register("mixing_bowl_be", () -> BlockEntityType.Builder.of(
                    MixingBowlBlockEntity::new, ModBlocks.MIXING_BOWL.get()).build(null));

    public static final Supplier<BlockEntityType<OvenBlockEntity>> OVEN_BE =
            BLOCK_ENTITIES.register("oven_be", () -> BlockEntityType.Builder.of(
                    OvenBlockEntity::new,
                    ModBlocks.OVENS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)
            ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
