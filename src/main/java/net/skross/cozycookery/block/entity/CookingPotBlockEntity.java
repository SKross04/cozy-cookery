package net.skross.cozycookery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.block.custom.PotLiquid;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.recipe.CookingRecipe;
import net.skross.cozycookery.recipe.CookingRecipeInput;
import net.skross.cozycookery.recipe.ModRecipes;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookingPotBlockEntity extends BlockEntity {
    public static final int INGREDIENT_SLOTS = 8;
    public static final int SERVINGS_WHEN_BURNT = 2;
    public static final int WATER_COLOR = 0x3F76E4; // the colour of plain water in the pot

    // Stirring: the pot asks for a stir every 8-12 seconds and you have 6 seconds to do it
    private static final int STIR_INTERVAL_MIN = 160;   // 8 seconds
    private static final int STIR_INTERVAL_RANDOM = 80; // + up to 4 seconds
    private static final int STIR_WINDOW = 120;         // 6 seconds

    // The ingredients in the pot: 6 slots with 1 item each
    public final ItemStackHandler ingredients = new ItemStackHandler(INGREDIENT_SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            syncToClient();
        }
    };

    // ---------- Cooking state ----------
    @Nullable
    private ResourceLocation cookingRecipeId = null; // which recipe is cooking right now
    private int progress = 0;          // ticks cooked so far
    private int nextStirIn = 0;        // ticks until the pot asks for a stir
    private boolean needsStir = false; // big bubbles: stir now!
    private int stirTimeLeft = 0;      // ticks left to stir before it burns
    private int cookingTime = 0;       // total ticks the current recipe needs
    private int targetColor = WATER_COLOR; // colour the water slowly turns into while cooking

    // ---------- Finished dish ----------
    private ItemStack serving = ItemStack.EMPTY; // 1 serving of the dish
    private int servings = 0;                     // how many bowls are left
    private int soupColor = WATER_COLOR;          // colour of the finished soup

    public CookingPotBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COOKING_POT_BE.get(), pos, blockState);
    }

    // ---------- Ingredients ----------
    public boolean addIngredient(ItemStack stack) {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            if (ingredients.getStackInSlot(i).isEmpty()) {
                ingredients.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    public ItemStack removeLastIngredient() {
        for (int i = INGREDIENT_SLOTS - 1; i >= 0; i--) {
            if (!ingredients.getStackInSlot(i).isEmpty()) {
                return ingredients.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasIngredients() {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            if (!ingredients.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void clearIngredients() {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            ingredients.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    private Optional<RecipeHolder<CookingRecipe>> findRecipe(Level level) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            ItemStack stack = ingredients.getStackInSlot(i);
            if (!stack.isEmpty()) {
                items.add(stack);
            }
        }
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(ModRecipes.COOKING_TYPE.get(), new CookingRecipeInput(items), level);
    }

    // ---------- Ticking (server only, 20 times per second) ----------
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(CookingPotBlock.LIQUID) != PotLiquid.WATER) {
            return;
        }
        Optional<RecipeHolder<CookingRecipe>> recipe = findRecipe(level);

        // No recipe matches (yet) -> nothing is cooking
        if (recipe.isEmpty()) {
            resetCooking();
            return;
        }
        // Ingredients changed to another recipe -> start over
        if (!recipe.get().id().equals(cookingRecipeId)) {
            resetCooking();
            cookingRecipeId = recipe.get().id();
            cookingTime = recipe.get().value().cookingTime();
            targetColor = recipe.get().value().color();
            nextStirIn = randomStirInterval(level);
            syncToClient();
        }
        // Safety net for pots saved before cooking_time existed
        if (cookingTime <= 0) {
            cookingTime = recipe.get().value().cookingTime();
            targetColor = recipe.get().value().color();
        }
        // No heat -> cooking pauses (progress is kept)
        if (!CookingPotBlock.isHeated(level, pos)) {
            return;
        }

        progress++;

        if (needsStir) {
            stirTimeLeft--;
            if (stirTimeLeft % 20 == 0) {
                level.playSound(null, pos, ModSounds.COOKING_POT_BLUB.get(), SoundSource.BLOCKS,
                        1.0f, 0.8f + level.getRandom().nextFloat() * 0.3f);
            }
            if (stirTimeLeft <= 0) {
                burn((ServerLevel) level, pos, state);
                return;
            }
        } else {
            nextStirIn--;
            // Ask for a stir, but not in the last 3 seconds
            if (nextStirIn <= 0 && progress < cookingTime - 60) {
                needsStir = true;
                stirTimeLeft = STIR_WINDOW;
                level.playSound(null, pos, ModSounds.COOKING_POT_BLUB.get(), SoundSource.BLOCKS, 1.2f, 0.8f);
                syncToClient();
            }
        }

        if (progress >= cookingTime) {
            finish(level, pos, state, recipe.get().value());
        } else if (progress % 20 == 0) {
            syncToClient(); // once per second, so the client can slowly change the water colour
        }
    }

    // Called when the player clicks with a spoon. Returns true if the pot really needed a stir.
    public boolean stir(Level level) {
        if (!needsStir) {
            return false;
        }
        needsStir = false;
        nextStirIn = randomStirInterval(level);
        syncToClient();
        return true;
    }

    private void finish(Level level, BlockPos pos, BlockState state, CookingRecipe recipe) {
        serving = recipe.result().copyWithCount(1);
        servings = recipe.result().getCount();
        soupColor = recipe.color();
        clearIngredients();
        resetCooking();
        level.setBlock(pos, state.setValue(CookingPotBlock.LIQUID, PotLiquid.SOUP), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        syncToClient();
    }

    private void burn(ServerLevel level, BlockPos pos, BlockState state) {
        serving = new ItemStack(ModItems.BURNT_STEW.get());
        servings = SERVINGS_WHEN_BURNT;
        clearIngredients();
        resetCooking();
        level.setBlock(pos, state.setValue(CookingPotBlock.LIQUID, PotLiquid.BURNT), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 0.8f);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                12, 0.2, 0.1, 0.2, 0.02);
        syncToClient();
    }

    private void resetCooking() {
        cookingRecipeId = null;
        progress = 0;
        cookingTime = 0;
        targetColor = WATER_COLOR;
        needsStir = false;
        stirTimeLeft = 0;
    }

    private static int randomStirInterval(Level level) {
        return STIR_INTERVAL_MIN + level.getRandom().nextInt(STIR_INTERVAL_RANDOM + 1);
    }

    // ---------- Serving ----------
    public boolean hasServings() {
        return servings > 0 && !serving.isEmpty();
    }

    // Takes 1 serving out. When the last one is taken, the pot becomes empty again.
    public ItemStack takeServing(Level level, BlockPos pos, BlockState state) {
        if (!hasServings()) {
            return ItemStack.EMPTY;
        }
        ItemStack result = serving.copy();
        servings--;
        if (servings <= 0) {
            serving = ItemStack.EMPTY;
            level.setBlock(pos, state.setValue(CookingPotBlock.LIQUID, PotLiquid.EMPTY), Block.UPDATE_ALL);
        }
        syncToClient();
        return result;
    }

    public boolean needsStir() {
        return needsStir;
    }

    // ---------- Colour (used by the colour handler in CozyCookeryClient) ----------
    // Finished soup -> its colour. Cooking -> water slowly blends into the soup colour. Otherwise plain water.
    public int getLiquidColor() {
        PotLiquid liquid = getBlockState().getValue(CookingPotBlock.LIQUID);
        if (liquid == PotLiquid.SOUP) {
            return soupColor;
        }
        if (liquid == PotLiquid.WATER && cookingRecipeId != null && cookingTime > 0) {
            float amount = Math.min(1f, (float) progress / cookingTime);
            return FastColor.ARGB32.lerp(amount, WATER_COLOR, targetColor);
        }
        return WATER_COLOR;
    }

    // ---------- Helpers ----------
    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(ingredients.getSlots());
        for (int i = 0; i < ingredients.getSlots(); i++) {
            inv.setItem(i, ingredients.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    // ---------- Saving & loading ----------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("ingredients", ingredients.serializeNBT(registries));
        if (cookingRecipeId != null) {
            tag.putString("recipe", cookingRecipeId.toString());
        }
        tag.putInt("progress", progress);
        tag.putInt("next_stir_in", nextStirIn);
        tag.putBoolean("needs_stir", needsStir);
        tag.putInt("stir_time_left", stirTimeLeft);
        tag.putInt("cooking_time", cookingTime);
        tag.putInt("target_color", targetColor);
        tag.putInt("soup_color", soupColor);
        tag.putInt("servings", servings);
        if (!serving.isEmpty()) {
            tag.put("serving", serving.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ingredients.deserializeNBT(registries, tag.getCompound("ingredients"));
        cookingRecipeId = tag.contains("recipe") ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        progress = tag.getInt("progress");
        nextStirIn = tag.getInt("next_stir_in");
        needsStir = tag.getBoolean("needs_stir");
        stirTimeLeft = tag.getInt("stir_time_left");
        cookingTime = tag.getInt("cooking_time");
        targetColor = tag.contains("target_color") ? tag.getInt("target_color") : WATER_COLOR;
        soupColor = tag.contains("soup_color") ? tag.getInt("soup_color") : WATER_COLOR;
        servings = tag.getInt("servings");
        serving = tag.contains("serving")
                ? ItemStack.parse(registries, tag.getCompound("serving")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    // ---------- Syncing server -> client ----------
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // When new data arrives on the client, redraw the block so the new soup colour shows up right away
    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
