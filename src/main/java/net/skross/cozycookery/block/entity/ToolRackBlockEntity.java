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
import net.skross.cozycookery.item.custom.KnifeItem;
import net.skross.cozycookery.item.custom.SpatulaItem;
import net.skross.cozycookery.item.custom.SpoonItem;
import org.jetbrains.annotations.Nullable;

// A wall rack with 4 hooks for kitchen tools (knives, spoon, spatula)
public class ToolRackBlockEntity extends BlockEntity {
    public static final int SLOTS = 4;

    public final ItemStackHandler tools = new ItemStackHandler(SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isKitchenTool(stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            toolsChanged();
        }
    };

    public ToolRackBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TOOL_RACK_BE.get(), pos, blockState);
    }

    public static boolean isKitchenTool(ItemStack stack) {
        return stack.getItem() instanceof KnifeItem || stack.getItem() instanceof SpoonItem
                || stack.getItem() instanceof SpatulaItem;
    }

    public ItemStack getTool(int slot) {
        return tools.getStackInSlot(slot);
    }

    // Saves and tells the clients (also called when a tool on the rack loses durability)
    public void toolsChanged() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            inv.setItem(i, tools.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("tools", tools.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tools.deserializeNBT(registries, tag.getCompound("tools"));
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
