package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.item.Items;
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
import net.skross.cozycookery.block.entity.MixingBowlBlockEntity;
import net.skross.cozycookery.item.custom.SpoonItem;
import net.skross.cozycookery.recipe.MixingRecipe;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MixingBowlBlock extends BaseEntityBlock {
    public static final MapCodec<MixingBowlBlock> CODEC = simpleCodec(MixingBowlBlock::new);
    // A wooden bowl: 10 x 10 pixels, 6 pixels high
    private static final VoxelShape SHAPE = Block.box(3, 0, 3, 13, 6, 13);

    public MixingBowlBlock(Properties properties) {
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

        // Spoon -> stir. If the ingredients match a recipe, they become dough.
        if (stack.getItem() instanceof SpoonItem) {
            if (bowl.isEmpty() || bowl.isMixed()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                level.playSound(null, pos, ModSounds.COOKING_POT_STIR.get(), SoundSource.BLOCKS,
                        1.0f, 1.1f + level.getRandom().nextFloat() * 0.2f);
                Optional<MixingRecipe> recipe = bowl.findRecipe(level);
                if (recipe.isPresent()) {
                    ItemStack particleItem = bowl.items.getStackInSlot(0).copy();
                    bowl.mix(recipe.get());
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    ((ServerLevel) level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleItem),
                            pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5, 8, 0.15, 0.05, 0.15, 0.03);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Tools and empty buckets are no ingredients (milk buckets, water bottles and empty bowls for salads are!)
        if (bowl.isMixed() || stack.isDamageableItem() || stack.is(Items.BUCKET)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Anything else -> into the bowl (max 8). Containers come back: milk bucket -> bucket, water bottle -> glass bottle.
        if (!level.isClientSide()) {
            if (bowl.addIngredient(stack)) {
                ItemStack container = stack.is(Items.POTION) ? new ItemStack(Items.GLASS_BOTTLE) : stack.getCraftingRemainingItem();
                stack.consume(1, player);
                if (!container.isEmpty()) {
                    player.getInventory().placeItemBackInInventory(container);
                }
                level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.5f, 1.4f);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    // Empty hand -> take the dough out (or the last ingredient you put in)
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl && !bowl.isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack taken = bowl.takeItem();
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, taken);
                } else {
                    player.getInventory().placeItemBackInInventory(taken);
                }
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.4f, 1.2f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }
}
