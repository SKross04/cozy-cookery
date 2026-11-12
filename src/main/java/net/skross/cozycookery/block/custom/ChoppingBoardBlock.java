package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeHolder;
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
import net.skross.cozycookery.recipe.ChoppingRecipe;
import net.skross.cozycookery.recipe.ChoppingRecipeInput;
import net.skross.cozycookery.recipe.ModRecipes;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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

        // Board is full -> is there a chopping recipe for this item + the tool in your hand?
        Optional<ChoppingRecipe> recipe = findRecipe(level, board.getItem(), stack);
        if (recipe.isPresent()) {
            if (!level.isClientSide()) {
                chop(recipe.get(), board, (ServerLevel) level, pos, player, hand, stack);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // No recipe -> let useWithoutItem decide (takes the item off)
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // ---------- Chopping ----------
    private static Optional<ChoppingRecipe> findRecipe(Level level, ItemStack onBoard, ItemStack heldItem) {
        return level.getRecipeManager()
                .getRecipesFor(ModRecipes.CHOPPING_TYPE.get(), new ChoppingRecipeInput(onBoard), level)
                .stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.matchesTool(heldItem))
                .findFirst();
    }

    private static void chop(ChoppingRecipe recipe, ChoppingBoardBlockEntity board, ServerLevel level,
                             BlockPos pos, Player player, InteractionHand hand, ItemStack tool) {
        ItemStack input = board.getItem().copy();

        if (recipe.hasSingleResult()) {
            // 1 result -> it stays on the board, so you can keep chopping
            board.inventory.setStackInSlot(0, recipe.results().get(0).copy());
        } else {
            // Several results (chicken -> 2 drumsticks + 2 wings) -> they pop out of the board
            board.inventory.setStackInSlot(0, ItemStack.EMPTY);
            for (ItemStack result : recipe.results()) {
                Block.popResource(level, pos, result.copy());
            }
        }

        // Chop sound with a slightly random pitch
        level.playSound(null, pos, ModSounds.CHOPPING_BOARD_CHOP.get(), SoundSource.BLOCKS,
                1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);

        // Little pieces of the chopped item fly off the board
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, input),
                pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                8, 0.15, 0.05, 0.15, 0.05);

        // Every chop costs 1 durability of the knife
        tool.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
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
