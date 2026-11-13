package net.skross.cozycookery.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.entity.ChoppingBoardBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

// Chopping board close-up: the 1 spot on the board + your inventory
public class ChoppingBoardMenu extends StationMenu {
    public final ChoppingBoardBlockEntity board;

    public ChoppingBoardMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (ChoppingBoardBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()),
                extraData.readEnum(CloseUpMode.class), ToolAccess.read(extraData));
    }

    public ChoppingBoardMenu(int containerId, Inventory inventory, ChoppingBoardBlockEntity board, CloseUpMode mode,
                             ToolAccess tool) {
        super(ModMenuTypes.CHOPPING_BOARD_MENU.get(), containerId, board, mode, tool);
        this.board = board;
        addStationSlot(new SlotItemHandler(board.inventory, 0, slotX(0), slotY(0)));
        addPlayerInventory(inventory);
    }
}
