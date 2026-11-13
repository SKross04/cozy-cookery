package net.skross.cozycookery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.CozyCookery;
import net.skross.cozycookery.block.entity.PlateBlockEntity;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.menu.PlateMenu;
import net.skross.cozycookery.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An empty plate on the counter. Right-click to lean in and arrange the parts of a meal on it; as soon as they
 * match a plating recipe (the same ones as in the crafting table) the plate turns into that plated meal.
 */
public class PlateBlock extends BaseEntityBlock {
    public static final MapCodec<PlateBlock> CODEC = simpleCodec(PlateBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 2, 14);

    public PlateBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlateBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock() && level.getBlockEntity(pos) instanceof PlateBlockEntity plate) {
            plate.drops();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION; // -> useWithoutItem: the close-up
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PlateBlockEntity plate)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                            (containerId, inventory, p) -> new PlateMenu(containerId, inventory, plate),
                            Component.translatable("block.cozycookery.plate")),
                    buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * Do the parts on the plate (+ the plate itself) match a plating recipe? Then the plate becomes the meal.
     * Parts that came in a bowl give the bowl back. Called on the server after every change in the close-up.
     */
    public static void tryAssemble(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof PlateBlockEntity plate)) {
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < PlateBlockEntity.SLOTS; i++) {
            ItemStack part = plate.parts.getStackInSlot(i);
            if (!part.isEmpty()) {
                items.add(part);
            }
        }
        if (items.isEmpty()) {
            return;
        }
        items.add(new ItemStack(ModItems.PLATE.get()));
        CraftingInput input = CraftingInput.of(items.size(), 1, items);
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            ResourceLocation id = holder.id();
            if (!id.getNamespace().equals(CozyCookery.MOD_ID) || !id.getPath().startsWith("plating/")
                    || !holder.value().matches(input, level)) {
                continue;
            }
            ItemStack result = holder.value().assemble(input, level.registryAccess());
            if (!(result.getItem() instanceof BlockItem blockItem)) {
                return;
            }
            for (ItemStack part : items) {
                if (part.is(ModTags.Items.PLATED_BOWL_DISHES)) {
                    player.getInventory().placeItemBackInInventory(new ItemStack(Items.BOWL));
                }
            }
            Direction facing = level.getBlockState(pos).getValue(FACING);
            plate.clear(); // so nothing drops when the plate block is replaced
            BlockState meal = blockItem.getBlock().defaultBlockState();
            if (meal.hasProperty(PlatedMealBlock.FACING)) {
                meal = meal.setValue(PlatedMealBlock.FACING, facing);
            }
            level.setBlock(pos, meal, Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0f, 0.8f);
            return; // the close-up closes by itself: its plate is gone (StationMenu.stillValid)
        }
    }
}
