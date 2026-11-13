package net.skross.cozycookery.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.entity.MortarBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

// Mortar close-up: the spot in the bowl + your inventory. The pestle belongs to the mortar, so no tool needed.
public class MortarMenu extends StationMenu {
    public final MortarBlockEntity mortar;

    public MortarMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (MortarBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public MortarMenu(int containerId, Inventory inventory, MortarBlockEntity mortar) {
        super(ModMenuTypes.MORTAR_MENU.get(), containerId, mortar, CloseUpMode.TOOL, ToolAccess.HAND);
        this.mortar = mortar;
        addStationSlot(new SlotItemHandler(mortar.inventory, 0, slotX(0), slotY(0)));
        addPlayerInventory(inventory);
    }
}
