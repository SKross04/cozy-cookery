package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.item.ModItems;

/**
 * A tomato plant growing on a 2-block trellis.
 * Same idea as vanilla's pitcher plant: a DoublePlantBlock (2 halves) + BonemealableBlock (bone meal) + its own AGE.
 * Both halves always have the same AGE. When ripe (age 7) you pick the tomatoes with a right-click,
 * and the plant goes back to age 5 (flowering) to grow new ones.
 */
public class TomatoTrellisBlock extends DoublePlantBlock implements BonemealableBlock {
    public static final MapCodec<TomatoTrellisBlock> CODEC = simpleCodec(TomatoTrellisBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    public static final int MAX_AGE = 7;
    private static final int AGE_AFTER_PICKING = 5;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public TomatoTrellisBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HALF, DoubleBlockHalf.LOWER)
                .setValue(AGE, 0));
    }

    @Override
    public MapCodec<? extends DoublePlantBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, AGE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    // ---------- Growing ----------
    // Only the bottom half ticks; it grows both halves at once
    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos, 0) >= 9 && random.nextInt(3) == 0) {
            setAge(level, pos, state, state.getValue(AGE) + 1);
        }
    }

    // Sets the age of BOTH halves (pos can be the top or the bottom half)
    private static void setAge(Level level, BlockPos pos, BlockState state, int age) {
        BlockPos lower = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
        int newAge = Math.min(MAX_AGE, age);
        BlockState lowerState = level.getBlockState(lower);
        BlockState upperState = level.getBlockState(lower.above());
        // UPDATE_KNOWN_SHAPE: change both halves without them checking each other in between
        // (otherwise DoublePlantBlock may think the other half is gone and remove the plant)
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        if (lowerState.hasProperty(AGE)) level.setBlock(lower, lowerState.setValue(AGE, newAge), flags);
        if (upperState.hasProperty(AGE)) level.setBlock(lower.above(), upperState.setValue(AGE, newAge), flags);
    }

    // ---------- Bone meal ----------
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        setAge(level, pos, state, state.getValue(AGE) + 1 + random.nextInt(2));
    }

    // ---------- Bone meal in your hand ----------
    // Handled here ourselves: growing -> grow, ripe -> do nothing (pick with an empty hand or another item)
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(Items.BONE_MEAL)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (state.getValue(AGE) >= MAX_AGE) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            performBonemeal((ServerLevel) level, level.getRandom(), pos, state);
            stack.consume(1, player);
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    // ---------- Picking (right-click when ripe) ----------
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (state.getValue(AGE) < MAX_AGE) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            Block.popResource(level, pos, new ItemStack(ModItems.TOMATO.get(), 1 + level.getRandom().nextInt(3)));
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS,
                    1.0f, 0.8f + level.getRandom().nextFloat() * 0.4f);
            setAge(level, pos, state, AGE_AFTER_PICKING);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // Middle mouse button in creative gives the seeds
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(ModItems.TOMATO_SEEDS.get());
    }
}
