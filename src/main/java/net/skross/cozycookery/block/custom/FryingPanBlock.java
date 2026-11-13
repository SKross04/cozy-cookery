package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.skross.cozycookery.util.ToolAccess;
import net.skross.cozycookery.menu.FryingPanMenu;
import net.skross.cozycookery.menu.CloseUpMode;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.entity.FryingPanBlockEntity;
import net.skross.cozycookery.block.entity.ModBlockEntities;
import net.skross.cozycookery.item.custom.SpatulaItem;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

public class FryingPanBlock extends BaseEntityBlock {
    public static final MapCodec<FryingPanBlock> CODEC = simpleCodec(FryingPanBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // The pan itself is 10 x 10 pixels and 2 pixels high (the handle is not part of the hitbox)
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 2, 13);

    public FryingPanBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
        builder.add(FACING);
    }

    // ---------- Block entity + ticker ----------
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FryingPanBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.FRYING_PAN_BE.get(),
                (tickLevel, tickPos, tickState, pan) -> pan.tick(tickLevel, tickPos, tickState));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof FryingPanBlockEntity pan) {
                pan.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ---------- Interaction (no GUI!) ----------
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof FryingPanBlockEntity pan)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Spatula -> lean in with the spatula (flick to flip, see FryingPanScreen)
        if (stack.getItem() instanceof SpatulaItem) {
            if (!level.isClientSide()) {
                openCloseUp(player, pan, ToolAccess.HAND);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        // Anything else -> useWithoutItem opens the close-up
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Bare hand (or food): lean in. A spatula on a tool rack nearby is used automatically.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FryingPanBlockEntity pan)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            openCloseUp(player, pan, ToolAccess.find(player, pos, s -> s.getItem() instanceof SpatulaItem));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void openCloseUp(Player player, FryingPanBlockEntity pan, @Nullable ToolAccess tool) {
        CloseUpMode mode = tool == null ? CloseUpMode.ADD : CloseUpMode.TOOL;
        ToolAccess access = tool == null ? ToolAccess.HAND : tool;
        player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, p) -> new FryingPanMenu(containerId, inventory, pan, mode, access),
                        Component.translatable("block.cozycookery.frying_pan")),
                buf -> {
                    buf.writeBlockPos(pan.getBlockPos());
                    buf.writeEnum(mode);
                    access.write(buf);
                });
    }

    // ---------- Client effects: sizzling smoke, more when something needs flipping ----------
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!CookingPotBlock.isHeated(level, pos) || !(level.getBlockEntity(pos) instanceof FryingPanBlockEntity pan)
                || pan.isEmpty()) {
            return;
        }
        double y = pos.getY() + 2.5 / 16.0;
        double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
        double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.02, 0.0);

        if (pan.anyNeedsFlip()) {
            for (int i = 0; i < 3; i++) {
                level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.3 + random.nextDouble() * 0.4, y,
                        pos.getZ() + 0.3 + random.nextDouble() * 0.4, 0.0, 0.05, 0.0);
            }
            level.addParticle(ParticleTypes.CRIT, x, y, z, (random.nextDouble() - 0.5) * 0.2, 0.15,
                    (random.nextDouble() - 0.5) * 0.2); // hot oil spatters
        }
        if (random.nextInt(6) == 0) {
            level.playLocalSound(pos.getX() + 0.5, y, pos.getZ() + 0.5, ModSounds.FRYING_PAN_SIZZLE.get(),
                    SoundSource.BLOCKS, 0.4f, 0.9f + random.nextFloat() * 0.3f, false);
        }
    }
}
