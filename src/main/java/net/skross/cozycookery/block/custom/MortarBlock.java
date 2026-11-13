package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.skross.cozycookery.menu.MortarMenu;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.entity.MortarBlockEntity;
import net.skross.cozycookery.recipe.GrindingRecipe;
import net.skross.cozycookery.recipe.ModRecipes;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MortarBlock extends BaseEntityBlock {
    public static final MapCodec<MortarBlock> CODEC = simpleCodec(MortarBlock::new);
    // A small stone bowl: 8 x 8 pixels, 5 pixels high
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 5, 12);

    public MortarBlock(Properties properties) {
        super(properties);
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
        return new MortarBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof MortarBlockEntity mortar) {
                mortar.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static Optional<GrindingRecipe> findRecipe(Level level, ItemStack stack) {
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.GRINDING_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value);
    }

    // ---------- Interaction (no GUI!) ----------
    // With something that can be ground, on an empty mortar -> put it in.
    // Anything else -> handled by useWithoutItem (grind or take out), so you can keep clicking with a stack in your hand.
    // Anything in your hand -> useWithoutItem opens the close-up
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Lean in over the mortar: drag something in, hold the mouse and grind in circles with the pestle
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof MortarBlockEntity mortar)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                            (containerId, inventory, p) -> new MortarMenu(containerId, inventory, mortar),
                            Component.translatable("block.cozycookery.mortar")),
                    buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // The ground result stays in the mortar; several results go to your inventory
    public static void grind(GrindingRecipe recipe, MortarBlockEntity mortar, ServerLevel level, BlockPos pos,
                             Player player) {
        ItemStack input = mortar.getItem().copy();
        ItemStack result = recipe.result().copy();
        if (result.getCount() == 1) {
            mortar.inventory.setStackInSlot(0, result);
        } else {
            mortar.inventory.setStackInSlot(0, ItemStack.EMPTY);
            player.getInventory().placeItemBackInInventory(result);
        }
        level.playSound(null, pos, ModSounds.MORTAR_GRIND.get(), SoundSource.BLOCKS,
                1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, input),
                pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, 6, 0.1, 0.05, 0.1, 0.03);
    }
}
