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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.skross.cozycookery.recipe.MixingRecipe;
import net.skross.cozycookery.recipe.MixingRecipeInput;
import net.skross.cozycookery.recipe.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MixingBowlBlockEntity extends BlockEntity {
    public static final int SLOTS = 8;

    // The ingredients in the bowl (1 item per slot). After mixing, slot 0 holds the dough.
    public final ItemStackHandler items = new ItemStackHandler(SLOTS) {
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

    private boolean mixed = false; // true when the bowl holds a finished dough

    public MixingBowlBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.MIXING_BOWL_BE.get(), pos, blockState);
    }

    public boolean isMixed() {
        return mixed;
    }

    public boolean isEmpty() {
        for (int i = 0; i < SLOTS; i++) {
            if (!items.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    public boolean addIngredient(ItemStack stack) {
        if (mixed) return false;
        for (int i = 0; i < SLOTS; i++) {
            if (items.getStackInSlot(i).isEmpty()) {
                items.setStackInSlot(i, stack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    // Takes the dough (the whole batch, e.g. 8 cookie doughs), or else the last ingredient that was put in
    public ItemStack takeItem() {
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items.getStackInSlot(i).isEmpty()) {
                int amount = mixed ? items.getStackInSlot(i).getCount() : 1;
                ItemStack taken = items.extractItem(i, amount, false);
                if (isEmpty()) mixed = false;
                return taken;
            }
        }
        return ItemStack.EMPTY;
    }

    public Optional<MixingRecipe> findRecipe(Level level) {
        if (mixed) return Optional.empty();
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < SLOTS; i++) {
            if (!items.getStackInSlot(i).isEmpty()) list.add(items.getStackInSlot(i));
        }
        if (list.isEmpty()) return Optional.empty();
        return level.getRecipeManager().getRecipeFor(ModRecipes.MIXING_TYPE.get(), new MixingRecipeInput(list), level)
                .map(RecipeHolder::value);
    }

    // Replaces the ingredients with the dough
    public void mix(MixingRecipe recipe) {
        for (int i = 0; i < SLOTS; i++) {
            items.setStackInSlot(i, ItemStack.EMPTY);
        }
        mixed = true;
        items.setStackInSlot(0, recipe.result().copy());
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(SLOTS);
        for (int i = 0; i < SLOTS; i++) inv.setItem(i, items.getStackInSlot(i));
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("items", items.serializeNBT(registries));
        tag.putBoolean("mixed", mixed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("items"));
        mixed = tag.getBoolean("mixed");
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
