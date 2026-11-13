package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.skross.cozycookery.item.custom.KnifeItem;
import net.skross.cozycookery.util.ToolAccess;
import net.skross.cozycookery.menu.ChoppingBoardMenu;
import net.skross.cozycookery.menu.CloseUpMode;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.network.chat.Component;
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

    // The board (with handle) is 15 x 10 pixels and 2 pixels high.
    // Block.box uses pixels: (minX, minY, minZ, maxX, maxY, maxZ) from 0 to 16.
    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0, 0, 3, 16, 2, 13);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(3, 0, 0, 13, 2, 16);

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
        // Knife -> lean in with the knife (click to chop, see ChoppingBoardScreen)
        if (stack.getItem() instanceof KnifeItem) {
            if (!level.isClientSide()) {
                openCloseUp(player, board, ToolAccess.HAND);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
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
        // Something on the board and something else in your hand -> useWithoutItem opens the close-up
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Bare hand: lean in. A knife on a tool rack nearby is used automatically.
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ChoppingBoardBlockEntity board)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            openCloseUp(player, board, ToolAccess.find(player, pos, s -> s.getItem() instanceof KnifeItem));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static void openCloseUp(Player player, ChoppingBoardBlockEntity board, @Nullable ToolAccess tool) {
        CloseUpMode mode = tool == null ? CloseUpMode.ADD : CloseUpMode.TOOL;
        ToolAccess access = tool == null ? ToolAccess.HAND : tool;
        player.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, p) -> new ChoppingBoardMenu(containerId, inventory, board, mode, access),
                        Component.translatable("block.cozycookery.oak_chopping_board")),
                buf -> {
                    buf.writeBlockPos(board.getBlockPos());
                    buf.writeEnum(mode);
                    access.write(buf);
                });
    }

    // ---------- Chopping ----------
    private static Optional<ChoppingRecipe> findRecipe(Level level, ItemStack onBoard, ItemStack tool) {
        return level.getRecipeManager()
                .getRecipesFor(ModRecipes.CHOPPING_TYPE.get(), new ChoppingRecipeInput(onBoard), level)
                .stream()
                .map(RecipeHolder::value)
                .filter(recipe -> recipe.matchesTool(tool))
                .findFirst();
    }

    /**
     * One cut. Results that stay a single item remain on the board; several results (chicken -> drumsticks + wings)
     * go to your inventory when you're in the close-up, or pop out of the board otherwise.
     * Returns true if something was cut.
     */
    public static boolean tryChop(ServerLevel level, BlockPos pos, ChoppingBoardBlockEntity board, Player player,
                                  ToolAccess tool, boolean toInventory) {
        ItemStack knife = tool.get(player);
        if (board.isEmpty() || knife.isEmpty()) {
            return false;
        }
        Optional<ChoppingRecipe> recipe = findRecipe(level, board.getItem(), knife);
        // the knife hits the board anyway (a dull "tock" when there's nothing to cut)
        if (recipe.isEmpty()) {
            level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.6f, 1.2f);
            return false;
        }
        ItemStack input = board.getItem().copy();
        if (recipe.get().hasSingleResult()) {
            board.inventory.setStackInSlot(0, recipe.get().results().get(0).copy());
        } else {
            board.inventory.setStackInSlot(0, ItemStack.EMPTY);
            for (ItemStack result : recipe.get().results()) {
                if (toInventory) {
                    player.getInventory().placeItemBackInInventory(result.copy());
                } else {
                    Block.popResource(level, pos, result.copy());
                }
            }
        }
        level.playSound(null, pos, ModSounds.CHOPPING_BOARD_CHOP.get(), SoundSource.BLOCKS,
                1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, input),
                pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 8, 0.15, 0.05, 0.15, 0.05);
        tool.damage(player); // every chop costs 1 durability
        return true;
    }
}
