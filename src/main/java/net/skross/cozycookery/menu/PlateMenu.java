package net.skross.cozycookery.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.custom.PlateBlock;
import net.skross.cozycookery.block.entity.PlateBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

// Plating close-up: the 4 spots on the plate + your inventory
public class PlateMenu extends StationMenu {
    public final PlateBlockEntity plate;

    public PlateMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (PlateBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PlateMenu(int containerId, Inventory inventory, PlateBlockEntity plate) {
        super(ModMenuTypes.PLATE_MENU.get(), containerId, plate, CloseUpMode.ADD, ToolAccess.HAND);
        this.plate = plate;
        Player player = inventory.player;
        for (int i = 0; i < PlateBlockEntity.SLOTS; i++) {
            addStationSlot(new SlotItemHandler(plate.parts, i, slotX(i), slotY(i)) {
                // every time you put something on the plate: is it a meal now?
                @Override
                public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
                    super.setByPlayer(newStack, oldStack);
                    PlateBlock.tryAssemble(player.level(), PlateMenu.this.pos, player);
                }
            });
        }
        addPlayerInventory(inventory);
    }
}
