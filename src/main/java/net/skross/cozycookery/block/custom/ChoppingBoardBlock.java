package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.entity.ChoppingBoardBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ChoppingBoardBlock extends BaseEntityBlock {
    public static final MapCodec<ChoppingBoardBlock> CODEC = simpleCodec(ChoppingBoardBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // The board is 14 x 10 pixels and 1 pixel high.
    // Block.box uses pixels: (minX, minY, minZ, maxX, maxY, maxZ) from 0 to 16.
    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(1, 0, 3, 15, 1, 13);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(3, 0, 1, 13, 1, 15);

    public ChoppingBoardBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ---------- Shape (hitbox) ----------
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_EAST_WEST : SHAPE_NORTH_SOUTH;
    }

    // ---------- Facing ----------
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The board faces the player who places it
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // ---------- Block entity ----------
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChoppingBoardBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // BaseEntityBlock is INVISIBLE by default, so we have to say "use my JSON model"
        return RenderShape.MODEL;
    }

    // When the board is broken, drop the item that was lying on it
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity board) {
                board.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ---------- Interaction (no GUI!) ----------

    // Right-click WITH an item in your hand
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity board)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Board is empty -> put 1 of the held item on it (anything can be placed, for decoration too)
        if (board.isEmpty()) {
            if (!level.isClientSide()) {
                board.inventory.insertItem(0, stack.copyWithCount(1), false);
                stack.consume(1, player); // removes 1 from your hand (not in creative)
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Board is full -> let useWithoutItem decide (takes the item off)
        // In step 4, the knife chop will go here.
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Right-click with an EMPTY hand (or when useItemOn passed)
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity board && !board.isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack taken = board.inventory.extractItem(0, 1, false);
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, taken);
                } else {
                    player.getInventory().placeItemBackInInventory(taken); // drops it if your inventory is full
                }
                level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }
}
