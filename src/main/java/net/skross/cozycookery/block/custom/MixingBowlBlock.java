package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.skross.cozycookery.util.ToolAccess;
import net.skross.cozycookery.menu.MixingBowlMenu;
import net.skross.cozycookery.menu.CloseUpMode;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.entity.MixingBowlBlockEntity;
import net.skross.cozycookery.component.ModDataComponents;
import net.skross.cozycookery.item.custom.SpoonItem;
import net.skross.cozycookery.recipe.MixingRecipe;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;
import java.util.Optional;

public class MixingBowlBlock extends BaseEntityBlock {
    public static final MapCodec<MixingBowlBlock> CODEC = simpleCodec(MixingBowlBlock::new);
    // A wooden bowl: 10 x 10 pixels, 6 pixels high
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 6, 13);

    // true = soup in the bowl (from a cooking pot): shows the soup surface in the model
    public static final BooleanProperty SOUP = BooleanProperty.create("soup");

    public MixingBowlBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SOUP, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOUP);
    }

    // Placing a mixing bowl that carries soup -> start with the soup surface visible
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(SOUP, context.getItemInHand().has(ModDataComponents.SOUP_CONTENTS.get()));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MixingBowlBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl) {
                bowl.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // ---------- Interaction (no GUI!) ----------
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Soup in the bowl: only an empty bowl does something (scoop 1 serving)
        if (bowl.hasSoup()) {
            if (!stack.is(Items.BOWL)) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                ItemStack serving = bowl.takeSoupServing(level, pos, state);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, serving));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Spoon -> lean in with the spoon: grab it and mix in circles (see MixingBowlScreen)
        if (stack.getItem() instanceof SpoonItem) {
            if (!level.isClientSide()) {
                openCloseUp(player, bowl, ToolAccess.HAND);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        // Anything else -> useWithoutItem opens the close-up (drag ingredients in there)
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Bare hand: lean in. A spoon on a tool rack nearby is used automatically. (Soup in the bowl: no close-up.)
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl) || bowl.hasSoup()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            openCloseUp(player, bowl, ToolAccess.find(player, pos, s -> s.getItem() instanceof SpoonItem));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void openCloseUp(Player player, MixingBowlBlockEntity bowl, @Nullable ToolAccess tool) {
        CloseUpMode mode = tool == null ? CloseUpMode.ADD : CloseUpMode.TOOL;
        ToolAccess access = tool == null ? ToolAccess.HAND : tool;
        player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, p) -> new MixingBowlMenu(containerId, inventory, bowl, mode, access),
                        Component.translatable("block.cozycookery.mixing_bowl")),
                buf -> {
                    buf.writeBlockPos(bowl.getBlockPos());
                    buf.writeEnum(mode);
                    access.write(buf);
                });
    }
}
