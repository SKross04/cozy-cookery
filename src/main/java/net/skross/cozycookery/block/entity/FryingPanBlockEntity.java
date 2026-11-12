package net.skross.cozycookery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.recipe.FryingRecipe;
import net.skross.cozycookery.recipe.FryingRecipeInput;
import net.skross.cozycookery.recipe.ModRecipes;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FryingPanBlockEntity extends BlockEntity {
    public static final int SLOTS = 4;
    private static final int FLIP_WINDOW = 100; // 5 seconds to flip before it chars

    // 4 pieces of food, 1 per slot
    public final ItemStackHandler items = new ItemStackHandler(SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            syncToClient();
        }
    };

    // Every piece has its own timer, like the 4 slots of a vanilla campfire
    private final int[] progress = new int[SLOTS];
    private final boolean[] needsFlip = new boolean[SLOTS];
    private final int[] flipTimeLeft = new int[SLOTS];
    private final boolean[] flipped = new boolean[SLOTS];

    public FryingPanBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.FRYING_PAN_BE.get(), pos, blockState);
    }

    // ---------- Recipes ----------
    public static Optional<RecipeHolder<FryingRecipe>> findRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(ModRecipes.FRYING_TYPE.get(), new FryingRecipeInput(stack), level);
    }

    public static boolean isFryable(Level level, ItemStack stack) {
        return findRecipe(level, stack).isPresent();
    }

    // ---------- Adding / taking ----------
    public boolean addItem(ItemStack stack) {
        for (int i = 0; i < SLOTS; i++) {
            if (items.getStackInSlot(i).isEmpty()) {
                resetSlot(i);
                items.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    // Takes a finished piece first; if nothing is finished, the last piece that was put in
    public ItemStack takeItem(Level level) {
        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && !isFryable(level, stack)) {
                resetSlot(i);
                return items.extractItem(i, 1, false);
            }
        }
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items.getStackInSlot(i).isEmpty()) {
                resetSlot(i);
                return items.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean isEmpty() {
        for (int i = 0; i < SLOTS; i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // ---------- Flipping ----------
    // Flips every piece that is waiting for it. Returns true if at least one piece was flipped.
    public boolean flip() {
        boolean any = false;
        for (int i = 0; i < SLOTS; i++) {
            if (needsFlip[i]) {
                needsFlip[i] = false;
                flipped[i] = true;
                any = true;
            }
        }
        if (any) {
            syncToClient();
        }
        return any;
    }

    public boolean anyNeedsFlip() {
        for (boolean b : needsFlip) {
            if (b) return true;
        }
        return false;
    }

    public boolean isFlipped(int slot) {
        return flipped[slot];
    }

    // ---------- Ticking (server only, 20 times per second) ----------
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!CookingPotBlock.isHeated(level, pos)) {
            return; // no heat -> nothing cooks (timers are kept)
        }
        boolean changed = false;

        for (int i = 0; i < SLOTS; i++) {
            ItemStack stack = items.getStackInSlot(i);
            Optional<RecipeHolder<FryingRecipe>> recipe = findRecipe(level, stack);
            if (recipe.isEmpty()) {
                continue; // empty slot, or already fried
            }
            int cookingTime = recipe.get().value().cookingTime();

            // Waiting to be flipped: the bottom is burning!
            if (needsFlip[i]) {
                flipTimeLeft[i]--;
                if (flipTimeLeft[i] % 20 == 0) {
                    level.playSound(null, pos, ModSounds.FRYING_PAN_SIZZLE.get(), SoundSource.BLOCKS,
                            1.0f, 1.2f + level.getRandom().nextFloat() * 0.2f);
                }
                if (flipTimeLeft[i] <= 0) {
                    resetSlot(i);
                    items.setStackInSlot(i, new ItemStack(ModItems.CHARRED_SCRAPS.get()));
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.2f);
                    ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 6, 0.15, 0.05, 0.15, 0.01);
                }
                continue;
            }

            progress[i]++;
            if (!flipped[i] && progress[i] >= cookingTime / 2) {
                needsFlip[i] = true;
                flipTimeLeft[i] = FLIP_WINDOW;
                level.playSound(null, pos, ModSounds.FRYING_PAN_SIZZLE.get(), SoundSource.BLOCKS, 1.2f, 1.3f);
                changed = true;
            } else if (progress[i] >= cookingTime) {
                resetSlot(i);
                items.setStackInSlot(i, recipe.get().value().result().copy());
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3f, 1.8f);
            }
        }

        if (changed) {
            syncToClient();
        } else if (level.getGameTime() % 20 == 0) {
            setChanged();
        }
    }

    private void resetSlot(int i) {
        progress[i] = 0;
        needsFlip[i] = false;
        flipTimeLeft[i] = 0;
        flipped[i] = false;
    }

    // ---------- Helpers ----------
    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(items.getSlots());
        for (int i = 0; i < items.getSlots(); i++) {
            inv.setItem(i, items.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    // ---------- Saving & loading ----------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("items", items.serializeNBT(registries));
        tag.putIntArray("progress", progress);
        tag.putIntArray("flip_time_left", flipTimeLeft);
        int[] needs = new int[SLOTS];
        int[] done = new int[SLOTS];
        for (int i = 0; i < SLOTS; i++) {
            needs[i] = needsFlip[i] ? 1 : 0;
            done[i] = flipped[i] ? 1 : 0;
        }
        tag.putIntArray("needs_flip", needs);
        tag.putIntArray("flipped", done);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("items"));
        int[] p = tag.getIntArray("progress");
        int[] f = tag.getIntArray("flip_time_left");
        int[] n = tag.getIntArray("needs_flip");
        int[] d = tag.getIntArray("flipped");
        for (int i = 0; i < SLOTS; i++) {
            progress[i] = i < p.length ? p[i] : 0;
            flipTimeLeft[i] = i < f.length ? f[i] : 0;
            needsFlip[i] = i < n.length && n[i] == 1;
            flipped[i] = i < d.length && d[i] == 1;
        }
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
}
