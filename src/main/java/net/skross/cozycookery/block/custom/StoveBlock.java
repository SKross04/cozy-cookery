package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import net.skross.cozycookery.block.entity.ModBlockEntities;
import net.skross.cozycookery.block.entity.StoveBlockEntity;
import org.jetbrains.annotations.Nullable;

public class StoveBlock extends BaseEntityBlock {
    public static final MapCodec<StoveBlock> CODEC = simpleCodec(StoveBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public StoveBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ---------- Facing (the firebox door faces the player) ----------
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
        builder.add(FACING, LIT);
    }

    // ---------- Block entity + ticker ----------
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StoveBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Tells Minecraft to call StoveBlockEntity.tick() every tick, on the server only
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.STOVE_BE.get(),
                (tickLevel, tickPos, tickState, stove) -> stove.tick(tickLevel, tickPos, tickState));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof StoveBlockEntity stove) {
                stove.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ---------- Interaction (no GUI!) ----------
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof StoveBlockEntity stove)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        boolean lit = state.getValue(LIT);

        // Coal, charcoal or a stick -> into the firebox
        if (StoveBlockEntity.isFuel(stack)) {
            if (!level.isClientSide()) {
                if (stove.addFuel(stack)) {
                    stack.consume(1, player);
                    level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 0.6f, 1.4f);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Flint and steel / fire charge -> light it
        if (!lit && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
            if (!level.isClientSide() && stove.ignite(level, pos, state)) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                } else {
                    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    stack.consume(1, player);
                }
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Shovel -> put the fire out (the fuel stays in)
        if (lit && stack.canPerformAction(ItemAbilities.SHOVEL_DOUSE)) {
            if (!level.isClientSide()) {
                extinguish(level, pos, state);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Water bucket -> put the fire out, you get the empty bucket back
        if (lit && stack.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide()) {
                extinguish(level, pos, state);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Empty hand on a stove that is NOT lit -> take the last fuel item back
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!state.getValue(LIT) && level.getBlockEntity(pos) instanceof StoveBlockEntity stove) {
            if (!level.isClientSide()) {
                ItemStack taken = stove.removeLastFuel();
                if (taken.isEmpty()) {
                    return InteractionResult.PASS;
                }
                player.getInventory().placeItemBackInInventory(taken);
                level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.6f, 1.2f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private static void extinguish(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state.setValue(LIT, false), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    // ---------- Client effects: crackling, smoke and little flames at the door ----------
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        Direction front = state.getValue(FACING);
        double x = pos.getX() + 0.5 + front.getStepX() * 0.52;
        double y = pos.getY() + 0.25;
        double z = pos.getZ() + 0.5 + front.getStepZ() * 0.52;
        double sideways = (random.nextDouble() - 0.5) * 0.4;

        if (random.nextInt(10) == 0) {
            level.playLocalSound(pos.getX() + 0.5, y, pos.getZ() + 0.5,
                    SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
        }
        double px = x + (front.getAxis() == Direction.Axis.Z ? sideways : 0);
        double pz = z + (front.getAxis() == Direction.Axis.X ? sideways : 0);
        level.addParticle(ParticleTypes.SMOKE, px, y + 0.15, pz, 0.0, 0.02, 0.0);
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, px, y, pz, 0.0, 0.0, 0.0);
        }
    }
}
