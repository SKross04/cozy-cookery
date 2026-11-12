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
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.skross.cozycookery.block.custom.OvenBlock;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.recipe.BakingRecipe;
import net.skross.cozycookery.recipe.ModRecipes;
import net.skross.cozycookery.sound.ModSounds;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class OvenBlockEntity extends BlockEntity {
    public static final int FUEL_SLOTS = 8;
    public static final int TRAY_SLOTS = 4;

    // ---------- Firebox: same fuel rules as the stove ----------
    public final ItemStackHandler fuel = new ItemStackHandler(FUEL_SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            syncToClient();
        }
    };
    private int burnTime = 0;

    // ---------- Tray: 4 spots, each with its own timer ----------
    public final ItemStackHandler tray = new ItemStackHandler(TRAY_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            syncToClient();
        }
    };
    private final int[] progress = new int[TRAY_SLOTS];  // ticks baked (raw item) or ticks too long (finished item)
    private final int[] bakeTime = new int[TRAY_SLOTS];  // how long the recipe took (to know when it burns)
    private final boolean[] done = new boolean[TRAY_SLOTS];

    public OvenBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OVEN_BE.get(), pos, blockState);
    }

    // ---------- Fuel (same as StoveBlockEntity) ----------
    public boolean addFuel(ItemStack stack) {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            if (fuel.getStackInSlot(i).isEmpty()) {
                fuel.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    public ItemStack removeLastFuel() {
        for (int i = FUEL_SLOTS - 1; i >= 0; i--) {
            if (!fuel.getStackInSlot(i).isEmpty()) {
                return fuel.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean burnNextFuel() {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = fuel.getStackInSlot(i);
            if (!stack.isEmpty()) {
                burnTime = StoveBlockEntity.getBurnDuration(stack);
                for (int j = i; j < FUEL_SLOTS - 1; j++) {
                    fuel.setStackInSlot(j, fuel.getStackInSlot(j + 1));
                }
                fuel.setStackInSlot(FUEL_SLOTS - 1, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    public boolean ignite(Level level, BlockPos pos, BlockState state) {
        if (burnTime <= 0 && !burnNextFuel()) {
            return false;
        }
        level.setBlock(pos, state.setValue(OvenBlock.LIT, true), Block.UPDATE_ALL);
        return true;
    }

    // ---------- Tray ----------
    public static Optional<BakingRecipe> findRecipe(Level level, ItemStack stack) {
        if (stack.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.BAKING_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value);
    }

    public boolean addToTray(ItemStack stack) {
        for (int i = 0; i < TRAY_SLOTS; i++) {
            if (tray.getStackInSlot(i).isEmpty()) {
                resetSlot(i);
                tray.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    // Takes something finished first; otherwise the last thing that was put on the tray
    public ItemStack takeFromTray() {
        for (int i = 0; i < TRAY_SLOTS; i++) {
            if (done[i] && !tray.getStackInSlot(i).isEmpty()) {
                ItemStack taken = tray.getStackInSlot(i).copy();
                resetSlot(i);
                tray.setStackInSlot(i, ItemStack.EMPTY);
                return taken;
            }
        }
        for (int i = TRAY_SLOTS - 1; i >= 0; i--) {
            if (!tray.getStackInSlot(i).isEmpty()) {
                ItemStack taken = tray.getStackInSlot(i).copy();
                resetSlot(i);
                tray.setStackInSlot(i, ItemStack.EMPTY);
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean trayIsEmpty() {
        for (int i = 0; i < TRAY_SLOTS; i++) {
            if (!tray.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    private void resetSlot(int i) {
        progress[i] = 0;
        bakeTime[i] = 0;
        done[i] = false;
    }

    // ---------- Ticking (server only) ----------
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(OvenBlock.LIT)) {
            return; // cold oven: nothing bakes or burns
        }

        // fire
        if (burnTime > 0) {
            burnTime--;
        }
        if (burnTime <= 0 && !burnNextFuel()) {
            level.setBlock(pos, state.setValue(OvenBlock.LIT, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.2f);
            return;
        }

        // tray
        for (int i = 0; i < TRAY_SLOTS; i++) {
            ItemStack stack = tray.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            if (done[i]) {
                // Finished, but still in the hot oven -> after 50% extra time it burns
                progress[i]++;
                if (progress[i] >= bakeTime[i] / 2) {
                    resetSlot(i);
                    tray.setStackInSlot(i, new ItemStack(ModItems.CHARRED_SCRAPS.get()));
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.0f);
                    ((ServerLevel) level).sendParticles(ParticleTypes.LARGE_SMOKE,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.1, 0.2, 0.01);
                }
                continue;
            }

            Optional<BakingRecipe> recipe = findRecipe(level, stack);
            if (recipe.isEmpty()) continue; // charred scraps or something that can't be baked
            progress[i]++;
            if (progress[i] >= recipe.get().cookingTime()) {
                bakeTime[i] = recipe.get().cookingTime();
                progress[i] = 0;
                done[i] = true;
                tray.setStackInSlot(i, recipe.get().result().copy());
                level.playSound(null, pos, ModSounds.OVEN_DING.get(), SoundSource.BLOCKS, 0.8f, 1.0f);
            }
        }
        if (level.getGameTime() % 20 == 0) {
            setChanged();
        }
    }

    // ---------- Helpers ----------
    private void syncToClient() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(FUEL_SLOTS + TRAY_SLOTS);
        for (int i = 0; i < FUEL_SLOTS; i++) inv.setItem(i, fuel.getStackInSlot(i));
        for (int i = 0; i < TRAY_SLOTS; i++) inv.setItem(FUEL_SLOTS + i, tray.getStackInSlot(i));
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    // ---------- Saving & loading ----------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fuel", fuel.serializeNBT(registries));
        tag.putInt("burn_time", burnTime);
        tag.put("tray", tray.serializeNBT(registries));
        tag.putIntArray("progress", progress);
        tag.putIntArray("bake_time", bakeTime);
        int[] d = new int[TRAY_SLOTS];
        for (int i = 0; i < TRAY_SLOTS; i++) d[i] = done[i] ? 1 : 0;
        tag.putIntArray("done", d);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuel.deserializeNBT(registries, tag.getCompound("fuel"));
        burnTime = tag.getInt("burn_time");
        tray.deserializeNBT(registries, tag.getCompound("tray"));
        int[] p = tag.getIntArray("progress");
        int[] b = tag.getIntArray("bake_time");
        int[] d = tag.getIntArray("done");
        for (int i = 0; i < TRAY_SLOTS; i++) {
            progress[i] = i < p.length ? p[i] : 0;
            bakeTime[i] = i < b.length ? b[i] : 0;
            done[i] = i < d.length && d[i] == 1;
        }
    }

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
