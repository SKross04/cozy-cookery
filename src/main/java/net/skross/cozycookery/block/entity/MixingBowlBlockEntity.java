package net.skross.cozycookery.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.skross.cozycookery.block.custom.MixingBowlBlock;
import net.skross.cozycookery.component.ModDataComponents;
import net.skross.cozycookery.component.SoupContents;
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

    // Soup poured in from a cooking pot (null = no soup)
    @Nullable
    private Item soupServing = null;
    private int soupServings = 0;
    private int soupColor = 0xFFFFFF;

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

    // ---------- Soup ----------
    public boolean hasSoup() {
        return soupServing != null && soupServings > 0;
    }

    public int getSoupColor() {
        return soupColor;
    }

    // Scoops 1 serving out (with an empty bowl). The last one turns the bowl back into an empty mixing bowl.
    public ItemStack takeSoupServing(Level level, BlockPos pos, BlockState state) {
        if (!hasSoup()) return ItemStack.EMPTY;
        ItemStack result = new ItemStack(soupServing);
        soupServings--;
        if (soupServings <= 0) {
            soupServing = null;
            level.setBlock(pos, state.setValue(MixingBowlBlock.SOUP, false), Block.UPDATE_ALL);
        }
        setChanged();
        level.sendBlockUpdated(pos, getBlockState(), getBlockState(), 3);
        return result;
    }

    // Item -> block: when a filled mixing bowl is placed, read the soup from the item's data component
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        SoupContents contents = componentInput.get(ModDataComponents.SOUP_CONTENTS.get());
        if (contents != null) {
            soupServing = contents.serving();
            soupServings = contents.servings();
            soupColor = contents.color();
        }
    }

    // Block -> item: when the bowl is broken, the loot table copies the soup back onto the item
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (hasSoup()) {
            components.set(ModDataComponents.SOUP_CONTENTS.get(), new SoupContents(soupServing, soupServings, soupColor));
        }
    }

    // The soup is now stored on the item as a component, so it doesn't need to be in the block's saved tag too
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        tag.remove("soup_item");
        tag.remove("soup_servings");
        tag.remove("soup_color");
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
        if (hasSoup()) {
            tag.putString("soup_item", BuiltInRegistries.ITEM.getKey(soupServing).toString());
            tag.putInt("soup_servings", soupServings);
            tag.putInt("soup_color", soupColor);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("items"));
        mixed = tag.getBoolean("mixed");
        if (tag.contains("soup_item")) {
            soupServing = BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("soup_item")));
            soupServings = tag.getInt("soup_servings");
            soupColor = tag.getInt("soup_color");
        } else {
            soupServing = null;
            soupServings = 0;
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

    // Redraw on the client when the soup changes (for the soup colour)
    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }
}
