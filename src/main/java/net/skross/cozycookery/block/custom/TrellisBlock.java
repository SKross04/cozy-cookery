package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.ModBlocks;
import net.skross.cozycookery.item.ModItems;

/**
 * An empty trellis: 2 blocks tall, placed on farmland.
 * DoublePlantBlock (vanilla, used for sunflowers and tall grass) already handles both halves:
 * placing the top half, breaking both together, and checking that the bottom stands on something valid.
 */
public class TrellisBlock extends DoublePlantBlock {
    public static final MapCodec<TrellisBlock> CODEC = simpleCodec(TrellisBlock::new);
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public TrellisBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // The bottom half can only stand on farmland
    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    // Tomato seeds on the trellis -> it becomes a (young) tomato plant on a trellis
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(ModItems.TOMATO_SEEDS.get())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockState plant = ModBlocks.TOMATO_TRELLIS.get().defaultBlockState();
            // UPDATE_KNOWN_SHAPE: don't let the two halves check each other while we swap them one by one
            int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            level.setBlock(lower, plant.setValue(HALF, DoubleBlockHalf.LOWER), flags);
            level.setBlock(lower.above(), plant.setValue(HALF, DoubleBlockHalf.UPPER), flags);
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(player, GameEvent.BLOCK_PLACE, pos);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
