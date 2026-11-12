package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.item.ModItems;

/**
 * A meal on a plate that you place and eat bite by bite, like a vanilla cake.
 * One class for all meals: each meal only differs in its hunger per bite (and its models/textures).
 * BITES 0 = untouched ... 3 = one bite left. After the last bite you get the plate back.
 */
public class PlatedMealBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty BITES = IntegerProperty.create("bites", 0, 3);
    public static final int LAST_BITE = 3;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 4, 14);

    private final int nutritionPerBite;
    private final float saturationPerBite;

    public PlatedMealBlock(Properties properties, int nutritionPerBite, float saturationPerBite) {
        super(properties);
        this.nutritionPerBite = nutritionPerBite;
        this.saturationPerBite = saturationPerBite;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(BITES, 0));
    }

    // Every meal has its own values, so this block can't be built from a simple codec
    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(properties -> new PlatedMealBlock(properties, nutritionPerBite, saturationPerBite));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ---------- Facing ----------
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
        builder.add(FACING, BITES);
    }

    // A meal needs something solid underneath (a table, a counter...)
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    // ---------- Eating (right-click, any hand) ----------
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!player.canEat(false)) {
            return InteractionResult.PASS; // not hungry
        }
        if (!level.isClientSide()) {
            player.getFoodData().eat(nutritionPerBite, saturationPerBite);
            level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f,
                    0.9f + level.getRandom().nextFloat() * 0.2f);
            level.gameEvent(player, GameEvent.EAT, pos);

            int bites = state.getValue(BITES);
            if (bites < LAST_BITE) {
                level.setBlock(pos, state.setValue(BITES, bites + 1), Block.UPDATE_ALL);
            } else {
                // Last bite: the meal is gone, you keep the plate
                level.removeBlock(pos, false);
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
                ItemStack plate = new ItemStack(ModItems.PLATE.get());
                if (!player.getInventory().add(plate)) {
                    player.drop(plate, false);
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
