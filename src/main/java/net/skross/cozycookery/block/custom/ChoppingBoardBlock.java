package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChoppingBoardBlock extends Block {
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
    protected MapCodec<? extends Block> codec() {
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
}
