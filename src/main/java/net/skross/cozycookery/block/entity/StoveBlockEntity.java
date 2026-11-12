package net.skross.cozycookery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.skross.cozycookery.block.custom.StoveBlock;
import org.jetbrains.annotations.Nullable;

public class StoveBlockEntity extends BlockEntity {
    public static final int FUEL_SLOTS = 8;

    // The firebox: 8 slots with 1 fuel item each (visible through the door)
    public final ItemStackHandler fuel = new ItemStackHandler(FUEL_SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int burnTime = 0; // ticks left for the fuel that is burning right now

    public StoveBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.STOVE_BE.get(), pos, blockState);
    }

    // ---------- Fuel ----------
    // How long one fuel item burns, in ticks (20 ticks = 1 second). 0 = not a fuel.
    public static int getBurnDuration(ItemStack stack) {
        if (stack.is(ItemTags.COALS)) return 1600;  // coal & charcoal: 80 seconds (same as a furnace)
        if (stack.is(Items.STICK)) return 200;      // stick: 10 seconds
        return 0;
    }

    public static boolean isFuel(ItemStack stack) {
        return getBurnDuration(stack) > 0;
    }

    // Puts 1 fuel item in the first free slot. Returns false when the firebox is full.
    public boolean addFuel(ItemStack stack) {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            if (fuel.getStackInSlot(i).isEmpty()) {
                fuel.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    // Takes the last fuel item back out (only used while the stove is not lit)
    public ItemStack removeLastFuel() {
        for (int i = FUEL_SLOTS - 1; i >= 0; i--) {
            if (!fuel.getStackInSlot(i).isEmpty()) {
                return fuel.extractItem(i, 1, false);
            }
        }
        return ItemStack.EMPTY;
    }

    // Burns the next fuel item (first in, first out). Returns false when there is no fuel left.
    private boolean burnNextFuel() {
        for (int i = 0; i < FUEL_SLOTS; i++) {
            ItemStack stack = fuel.getStackInSlot(i);
            if (!stack.isEmpty()) {
                burnTime = getBurnDuration(stack);
                // move the remaining fuel forward so the firebox fills from the front
                for (int j = i; j < FUEL_SLOTS - 1; j++) {
                    fuel.setStackInSlot(j, fuel.getStackInSlot(j + 1));
                }
                fuel.setStackInSlot(FUEL_SLOTS - 1, ItemStack.EMPTY);
                return true;
            }
        }
        return false;
    }

    // Called when the player uses flint and steel. Returns false when there is nothing to burn.
    public boolean ignite(Level level, BlockPos pos, BlockState state) {
        if (burnTime <= 0 && !burnNextFuel()) {
            return false;
        }
        level.setBlock(pos, state.setValue(StoveBlock.LIT, true), Block.UPDATE_ALL);
        return true;
    }

    // ---------- Ticking (server only, 20 times per second) ----------
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(StoveBlock.LIT)) {
            return;
        }
        if (burnTime > 0) {
            burnTime--;
            if (burnTime % 20 == 0) {
                setChanged();
            }
        }
        if (burnTime <= 0 && !burnNextFuel()) {
            // out of fuel -> the fire goes out
            level.setBlock(pos, state.setValue(StoveBlock.LIT, false), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.2f);
        }
    }

    // Drops the fuel when the stove is broken
    public void drops() {
        SimpleContainer inv = new SimpleContainer(fuel.getSlots());
        for (int i = 0; i < fuel.getSlots(); i++) {
            inv.setItem(i, fuel.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    // ---------- Saving & loading ----------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("fuel", fuel.serializeNBT(registries));
        tag.putInt("burn_time", burnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        fuel.deserializeNBT(registries, tag.getCompound("fuel"));
        burnTime = tag.getInt("burn_time");
    }

    // ---------- Syncing server -> client (for the fuel renderer) ----------
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
