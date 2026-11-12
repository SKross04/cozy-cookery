package net.skross.cozycookery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CookingPotBlockEntity extends BlockEntity {
    public static final int INGREDIENT_SLOTS = 6;

    // The ingredients in the pot: 6 slots with 1 item each
    public final ItemStackHandler ingredients = new ItemStackHandler(INGREDIENT_SLOTS) {
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

    public CookingPotBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.COOKING_POT_BE.get(), pos, blockState);
    }

    // ---------- Ingredients ----------
    // Puts 1 item in the first free slot. Returns false when the pot is full.
    public boolean addIngredient(ItemStack stack) {
        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            if (ingredients.getStackInSlot(i).isEmpty()) {
                ingredients.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    // Takes the last added ingredient back out
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

    // Drops the ingredients when the pot is broken
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
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ingredients.deserializeNBT(registries, tag.getCompound("ingredients"));
    }

    // ---------- Syncing server -> client (for the floating ingredients) ----------
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
