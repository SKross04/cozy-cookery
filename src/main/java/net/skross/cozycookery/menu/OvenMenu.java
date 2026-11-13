package net.skross.cozycookery.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.custom.OvenBlock;
import net.skross.cozycookery.block.entity.OvenBlockEntity;
import net.skross.cozycookery.block.entity.StoveBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

// Oven close-up: top row = the 4 spots on the baking tray, below = the 8 spots in the firebox
public class OvenMenu extends StationMenu {
    public final OvenBlockEntity oven;

    public OvenMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (OvenBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public OvenMenu(int containerId, Inventory inventory, OvenBlockEntity oven) {
        super(ModMenuTypes.OVEN_MENU.get(), containerId, oven, CloseUpMode.ADD, ToolAccess.HAND);
        this.oven = oven;
        for (int i = 0; i < OvenBlockEntity.TRAY_SLOTS; i++) {
            final int index = i;
            addStationSlot(new SlotItemHandler(oven.tray, i, slotX(i), slotY(i)) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return OvenBlockEntity.findRecipe(inventory.player.level(), stack).isPresent();
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }

                @Override
                public void set(ItemStack stack) {
                    OvenMenu.this.oven.resetSlot(index); // fresh timer for what you put in
                    super.set(stack);
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    OvenMenu.this.oven.resetSlot(index);
                    super.onTake(player, stack);
                }
            });
        }
        for (int i = 0; i < OvenBlockEntity.FUEL_SLOTS; i++) {
            int index = OvenBlockEntity.TRAY_SLOTS + i;
            addStationSlot(new SlotItemHandler(oven.fuel, i, slotX(index), slotY(index)) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return StoveBlockEntity.isFuel(stack);
                }

                // burning fuel stays in until the fire is out
                @Override
                public boolean mayPickup(Player player) {
                    return !OvenMenu.this.oven.getBlockState().getValue(OvenBlock.LIT);
                }
            });
        }
        addPlayerInventory(inventory);
    }
}
