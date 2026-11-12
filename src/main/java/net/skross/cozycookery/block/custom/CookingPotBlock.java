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
import net.minecraft.world.item.TieredItem;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.skross.cozycookery.block.ModBlocks;
import net.skross.cozycookery.block.entity.CookingPotBlockEntity;
import net.skross.cozycookery.block.entity.ModBlockEntities;
import net.skross.cozycookery.component.ModDataComponents;
import net.skross.cozycookery.item.custom.SpoonItem;
import net.skross.cozycookery.sound.ModSounds;
import net.skross.cozycookery.util.ModTags;
import org.jetbrains.annotations.Nullable;

public class CookingPotBlock extends BaseEntityBlock {
    public static final MapCodec<CookingPotBlock> CODEC = simpleCodec(CookingPotBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<PotLiquid> LIQUID = EnumProperty.create("liquid", PotLiquid.class);

    // The pot body is 12 x 12 pixels and 9 pixels high (handles are not part of the hitbox)
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 9, 14);

    public CookingPotBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIQUID, PotLiquid.EMPTY));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // ---------- Block entity ----------
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CookingPotBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // Tells Minecraft to call CookingPotBlockEntity.tick() every tick, on the server only
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, ModBlockEntities.COOKING_POT_BE.get(),
                (tickLevel, tickPos, tickState, pot) -> pot.tick(tickLevel, tickPos, tickState));
    }

    // When the pot is broken, drop the ingredients that were in it
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot) {
                pot.drops();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
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
        builder.add(FACING, LIQUID);
    }

    // ---------- Heat ----------
    // The pot is heated when the block below is in the heat_sources tag (and lit, if it can be lit)
    public static boolean isHeated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(ModTags.Blocks.HEAT_SOURCES)) {
            return false;
        }
        return !below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT);
    }

    // ---------- Interaction (no GUI!) ----------
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.isEmpty() || !(level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        PotLiquid liquid = state.getValue(LIQUID);

        // Water bucket into an empty pot -> fill with water
        if (stack.is(Items.WATER_BUCKET)) {
            if (liquid != PotLiquid.EMPTY) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
                level.setBlock(pos, state.setValue(LIQUID, PotLiquid.WATER), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Empty bucket on a pot with ONLY water (no ingredients) -> take the water back
        if (stack.is(Items.BUCKET)) {
            if (liquid != PotLiquid.WATER || pot.hasIngredients()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.WATER_BUCKET)));
                level.setBlock(pos, state.setValue(LIQUID, PotLiquid.EMPTY), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Spoon -> stir (only counts when the pot asks for it, then it costs 1 durability)
        if (stack.getItem() instanceof SpoonItem) {
            if (liquid != PotLiquid.WATER || !pot.hasIngredients()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                level.playSound(null, pos, ModSounds.COOKING_POT_STIR.get(), SoundSource.BLOCKS,
                        1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
                if (pot.stir(level)) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Empty mixing bowl on a finished soup -> pour ALL servings into it, to carry the soup around
        if (stack.is(ModBlocks.MIXING_BOWL.get().asItem()) && !stack.has(ModDataComponents.SOUP_CONTENTS.get())) {
            if (!pot.hasServings() || !pot.isServedInBowl()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION; // no soup: just place the mixing bowl
            }
            if (!level.isClientSide()) {
                ItemStack filled = new ItemStack(ModBlocks.MIXING_BOWL.get());
                filled.set(ModDataComponents.SOUP_CONTENTS.get(), pot.takeAllServings(level, pos, state));
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.8f, 1.3f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Bowl on a finished dish -> 1 serving
        if (stack.is(Items.BOWL)) {
            if (!pot.hasServings() || !pot.isServedInBowl()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                ItemStack serving = pot.takeServing(level, pos, state);
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, serving));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        // Any other item into a pot with water -> add it as an ingredient (max 6)
        if (liquid == PotLiquid.WATER && isIngredient(stack)) {
            if (!level.isClientSide()) {
                if (pot.addIngredient(stack)) {
                    stack.consume(1, player);
                    level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS,
                            0.3f, 1.3f + level.getRandom().nextFloat() * 0.3f);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    // Tools and containers are never ingredients (they get their own uses: spoon, bowl, knife...)
    private static boolean isIngredient(ItemStack stack) {
        return !(stack.getItem() instanceof TieredItem)
                && !(stack.getItem() instanceof SpoonItem)
                && !stack.is(Items.BOWL)
                && !stack.isDamageableItem();
    }

    // Empty hand -> take a finished dish that needs no bowl (cooked noodles), or else the last ingredient back out
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.hasServings() && !pot.isServedInBowl()) {
            if (!level.isClientSide()) {
                ItemStack serving = pot.takeServing(level, pos, state);
                if (player.getMainHandItem().isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, serving);
                } else {
                    player.getInventory().placeItemBackInInventory(serving);
                }
                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.3f, 1.5f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.hasIngredients()) {
            if (!level.isClientSide()) {
                ItemStack taken = pot.removeLastIngredient();
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

    // ---------- Client effects: bubbles, steam and bubbling sounds ----------
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIQUID) == PotLiquid.EMPTY || !isHeated(level, pos)) {
            return;
        }
        double surfaceY = pos.getY() + 7.2 / 16.0;

        // The pot asks for a stir: big bubbles, splashes and a burst of steam
        if (level.getBlockEntity(pos) instanceof CookingPotBlockEntity pot && pot.needsStir()) {
            for (int i = 0; i < 4; i++) {
                double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
                double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
                level.addParticle(ParticleTypes.SPLASH, x, surfaceY + 0.05, z, 0.0, 0.1, 0.0);
                level.addParticle(ParticleTypes.BUBBLE_POP, x, surfaceY, z, 0.0, 0.06, 0.0);
            }
            level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.5, surfaceY + 0.4, pos.getZ() + 0.5,
                    (random.nextDouble() - 0.5) * 0.02, 0.06, (random.nextDouble() - 0.5) * 0.02);
        }

        for (int i = 0; i < 2; i++) {
            double x = pos.getX() + 0.25 + random.nextDouble() * 0.5;
            double z = pos.getZ() + 0.25 + random.nextDouble() * 0.5;
            level.addParticle(ParticleTypes.BUBBLE_POP, x, surfaceY, z, 0.0, 0.02, 0.0);
        }
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + 0.3 + random.nextDouble() * 0.4;
            double z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
            level.addParticle(ParticleTypes.WHITE_SMOKE, x, surfaceY + 0.3, z, 0.0, 0.03, 0.0);
        }
        if (random.nextInt(8) == 0) {
            level.playLocalSound(pos.getX() + 0.5, surfaceY, pos.getZ() + 0.5,
                    SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundSource.BLOCKS,
                    0.5f, 0.8f + random.nextFloat() * 0.4f, false);
        }
    }
}
