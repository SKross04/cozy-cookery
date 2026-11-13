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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.entity.ToolRackBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A wooden rack on the wall with 4 hooks. Right-click with a knife, spoon or spatula to hang it on the hook you
 * point at, right-click with a bare hand to take it off. Stations nearby use the tools on it automatically.
 * FACING = the direction the front of the rack faces (away from the wall).
 */
public class ToolRackBlock extends BaseEntityBlock {
    public static final MapCodec<ToolRackBlock> CODEC = simpleCodec(ToolRackBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SOUTH = Block.box(1, 2, 0, 15, 13, 4);
    private static final VoxelShape NORTH = Block.box(1, 2, 12, 15, 13, 16);
    private static final VoxelShape EAST = Block.box(0, 2, 1, 4, 13, 15);
    private static final VoxelShape WEST = Block.box(12, 2, 1, 16, 13, 15);

    public ToolRackBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolRackBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Clicked on a wall -> hang on that wall. Clicked on a floor/ceiling -> face the player.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        Direction facing = face.getAxis().isHorizontal() ? face : context.getHorizontalDirection().getOpposite();
        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case WEST -> WEST;
            default -> SOUTH;
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof ToolRackBlockEntity rack) {
            rack.drops();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // Which hook (0-3, left to right as you look at the rack) the player clicked
    public static int hookAt(BlockState state, BlockPos pos, Vec3 hit) {
        double x = hit.x - pos.getX();
        double z = hit.z - pos.getZ();
        double fromLeft = switch (state.getValue(FACING)) {
            case SOUTH -> x;
            case NORTH -> 1 - x;
            case EAST -> 1 - z;
            default -> z; // WEST
        };
        return Math.max(0, Math.min(ToolRackBlockEntity.SLOTS - 1, (int) (fromLeft * ToolRackBlockEntity.SLOTS)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!ToolRackBlockEntity.isKitchenTool(stack) || !(level.getBlockEntity(pos) instanceof ToolRackBlockEntity rack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int hook = hookAt(state, pos, hitResult.getLocation());
        if (!rack.getTool(hook).isEmpty()) {
            hook = -1;
            for (int i = 0; i < ToolRackBlockEntity.SLOTS; i++) {
                if (rack.getTool(i).isEmpty()) {
                    hook = i;
                    break;
                }
            }
        }
        if (hook < 0) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION; // full
        }
        if (!level.isClientSide()) {
            rack.tools.setStackInSlot(hook, stack.copyWithCount(1));
            stack.consume(1, player);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8f, 1.2f);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ToolRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }
        int hook = hookAt(state, pos, hitResult.getLocation());
        if (rack.getTool(hook).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            ItemStack tool = rack.tools.extractItem(hook, 1, false);
            if (player.getMainHandItem().isEmpty()) {
                player.setItemInHand(InteractionHand.MAIN_HAND, tool);
            } else {
                player.getInventory().placeItemBackInInventory(tool);
            }
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.8f, 1.2f);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
