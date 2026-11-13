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
import net.skross.cozycookery.component.SoupContents;
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

    // Stirring (close-up with the spoon): keep stirring or it burns
    private static final int GRACE = 100;            // 5 s to grab the spoon after the ingredients are in
    private static final int WARN_AFTER = 80;        // 4 s without stirring -> it starts to stick and smoke
    private static final int BURN_AFTER = 140;       // 7 s without stirring -> Burnt Stew
    private static final int STIRRED_RECENTLY = 20;  // stirred in the last second -> cooks twice as fast

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
    private int neglect = 0;           // ticks since the last stir (starts negative = grace time)
    private long lastStirTick = -1000; // game time of the last stir
    private boolean sticking = false;  // not stirred for a while: smoke, hurry!
    private int cookingTime = 0;       // total ticks the current recipe needs
    private int targetColor = WATER_COLOR; // colour the water slowly turns into while cooking

    // ---------- Finished dish ----------
    private ItemStack serving = ItemStack.EMPTY; // 1 serving of the dish
    private int servings = 0;                     // how many bowls are left
    private int soupColor = WATER_COLOR;          // colour of the finished soup
    private boolean servedInBowl = true;          // false: take it out with an empty hand (cooked noodles)

    // ---------- Client only (close-up visuals, not saved) ----------
    public boolean clientSpoonIn = false;  // the spoon stands in the pot while you're in the spoon close-up
    public float clientSpoonAngle = 0.8f;  // where the spoon is, around the middle (radians)
    public float clientSwirl = 0;          // degrees the ingredients were pushed around by stirring

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

    // Close-up rules: ingredients go in when there is water and no finished dish,
    // and can come back out as long as cooking hasn't started
    public boolean canAddIngredients() {
        return getBlockState().getValue(CookingPotBlock.LIQUID) == PotLiquid.WATER && servings == 0;
    }

    public boolean canTakeIngredients() {
        return servings == 0 && progress == 0;
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
            neglect = -GRACE;
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

        boolean stirredRecently = level.getGameTime() - lastStirTick <= STIRRED_RECENTLY;
        progress += stirredRecently ? 2 : 1;
        neglect++;
        if (neglect >= BURN_AFTER) {
            burn((ServerLevel) level, pos, state);
            return;
        }
        boolean nowSticking = neglect >= WARN_AFTER;
        if (nowSticking != sticking) {
            sticking = nowSticking;
            syncToClient();
        }
        if (sticking && neglect % 20 == 0) {
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.6f);
        }

        if (progress >= cookingTime) {
            finish(level, pos, state, recipe.get().value());
        } else if (progress % 20 == 0) {
            syncToClient(); // once per second, so the client can slowly change the water colour
        }
    }

    // Called by the stir message from the close-up (StirPayload). Returns true if something is cooking.
    public boolean stir(Level level) {
        if (cookingRecipeId == null) {
            return false;
        }
        lastStirTick = level.getGameTime();
        neglect = 0;
        if (sticking) {
            sticking = false;
            syncToClient();
        }
        return true;
    }

    public boolean isCooking() {
        return cookingRecipeId != null;
    }

    private void finish(Level level, BlockPos pos, BlockState state, CookingRecipe recipe) {
        serving = recipe.result().copyWithCount(1);
        servings = recipe.result().getCount();
        soupColor = recipe.color();
        servedInBowl = recipe.needsBowl();
        clearIngredients();
        resetCooking();
        level.setBlock(pos, state.setValue(CookingPotBlock.LIQUID, PotLiquid.SOUP), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0f, 1.0f);
        syncToClient();
    }

    private void burn(ServerLevel level, BlockPos pos, BlockState state) {
        serving = new ItemStack(ModItems.BURNT_STEW.get());
        servings = SERVINGS_WHEN_BURNT;
        servedInBowl = true;
        soupColor = 0x2A2018; // dark, for when it's carried in a mixing bowl
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
        neglect = 0;
        sticking = false;
    }

    // ---------- Serving ----------
    public boolean hasServings() {
        return servings > 0 && !serving.isEmpty();
    }

    // Pours ALL servings into a mixing bowl (see CookingPotBlock). The pot becomes empty.
    public SoupContents takeAllServings(Level level, BlockPos pos, BlockState state) {
        SoupContents contents = new SoupContents(serving.getItem(), servings, soupColor);
        servings = 0;
        serving = ItemStack.EMPTY;
        level.setBlock(pos, state.setValue(CookingPotBlock.LIQUID, PotLiquid.EMPTY), Block.UPDATE_ALL);
        syncToClient();
        return contents;
    }

    public boolean isServedInBowl() {
        return servedInBowl;
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

    // Not stirred for a while: smoke and hissing (CookingPotBlock.animateTick, close-up hint)
    public boolean isSticking() {
        return sticking;
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
        tag.putInt("neglect", neglect);
        tag.putBoolean("sticking", sticking);
        tag.putInt("cooking_time", cookingTime);
        tag.putInt("target_color", targetColor);
        tag.putInt("soup_color", soupColor);
        tag.putBoolean("served_in_bowl", servedInBowl);
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
        neglect = tag.getInt("neglect");
        sticking = tag.getBoolean("sticking");
        cookingTime = tag.getInt("cooking_time");
        targetColor = tag.contains("target_color") ? tag.getInt("target_color") : WATER_COLOR;
        soupColor = tag.contains("soup_color") ? tag.getInt("soup_color") : WATER_COLOR;
        servedInBowl = !tag.contains("served_in_bowl") || tag.getBoolean("served_in_bowl");
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
