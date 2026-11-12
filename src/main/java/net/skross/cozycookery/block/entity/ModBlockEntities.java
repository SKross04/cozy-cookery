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

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
