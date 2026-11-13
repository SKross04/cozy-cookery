package net.skross.cozycookery.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.skross.cozycookery.util.ToolAccess;

/**
 * Shared base for the station close-ups (frying pan, chopping board...).
 * Layout: your inventory in a panel on the left, the station's own slots in a small panel on the right,
 * the station itself in the middle of the screen (seen through the close-up camera).
 */
public abstract class StationMenu extends AbstractContainerMenu {
    public static final int WIDTH = 330;
    public static final int HEIGHT = 180;
    public static final int PANEL_X = 248;
    public static final int PANEL_Y = 68;

    public final BlockPos pos;
    public final CloseUpMode mode;
    public final ToolAccess tool;
    private final BlockEntity station;
    private int stationSlots = 0;

    protected StationMenu(MenuType<?> type, int containerId, BlockEntity station, CloseUpMode mode, ToolAccess tool) {
        super(type, containerId);
        this.station = station;
        this.pos = station.getBlockPos();
        this.mode = mode;
        this.tool = tool;
    }

    // x/y of the station slot with this index in the right-hand panel (rows of 4)
    protected static int slotX(int index) {
        return PANEL_X + 6 + (index % 4) * 18;
    }

    protected static int slotY(int index) {
        return PANEL_Y + 16 + (index / 4) * 18;
    }

    // Call this first for every station slot, then addPlayerInventory
    protected void addStationSlot(Slot slot) {
        this.addSlot(slot);
        stationSlots++;
    }

    public int stationSlotCount() {
        return stationSlots;
    }

    protected void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 9; row++) {
            this.addSlot(new Slot(inventory, row, 10, 10 + row * 18));              // hotbar
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(inventory, 9 + row * 3 + col, 30 + col * 18, 10 + row * 18));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < stationSlots) {
            if (!this.moveItemStackTo(stack, stationSlots, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, stationSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return !station.isRemoved()
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
