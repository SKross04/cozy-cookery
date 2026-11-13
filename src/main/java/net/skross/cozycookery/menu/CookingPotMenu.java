package net.skross.cozycookery.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.block.entity.CookingPotBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

/**
 * The server side of the pot close-up. It links your inventory to the pot's 8 ingredient slots,
 * so dragging items around is checked by the server (no duplicating or losing items in multiplayer).
 * Slot positions are only used by the client screen (CookingPotScreen).
 */
public class CookingPotMenu extends AbstractContainerMenu {
    // Screen layout (pixels, relative to the screen's top-left corner)
    public static final int WIDTH = 330;
    public static final int HEIGHT = 180;
    public static final int POT_CENTER_X = 165;
    public static final int POT_CENTER_Y = 90;
    // "In the pot" panel on the right: 2 rows of 4
    public static final int POT_PANEL_X = 248;
    public static final int POT_PANEL_Y = 68;

    private static final int POT_SLOTS = CookingPotBlockEntity.INGREDIENT_SLOTS;

    public final CookingPotBlockEntity pot;
    public final BlockPos pos;
    public final CloseUpMode mode;
    public final ToolAccess tool; // where the spoon comes from (your hand or a tool rack)

    // Client side: the server sent the pot position, the mode and where the tool is
    public CookingPotMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (CookingPotBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()),
                extraData.readEnum(CloseUpMode.class), ToolAccess.read(extraData));
    }

    // Server side
    public CookingPotMenu(int containerId, Inventory inventory, CookingPotBlockEntity pot, CloseUpMode mode,
                          ToolAccess tool) {
        super(ModMenuTypes.COOKING_POT_MENU.get(), containerId);
        this.pot = pot;
        this.pos = pot.getBlockPos();
        this.mode = mode;
        this.tool = tool;

        // The pot's ingredients: a small "In the pot" panel on the right, so the pot itself stays free
        for (int i = 0; i < POT_SLOTS; i++) {
            int x = POT_PANEL_X + 6 + (i % 4) * 18;
            int y = POT_PANEL_Y + 16 + (i / 4) * 18;
            this.addSlot(new SlotItemHandler(pot.ingredients, i, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return CookingPotMenu.this.pot.canAddIngredients() && CookingPotBlock.isIngredient(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return CookingPotMenu.this.pot.canTakeIngredients();
                }
            });
        }

        // Your inventory: a panel on the left, 4 columns x 9 rows (first column = hotbar)
        for (int row = 0; row < 9; row++) {
            this.addSlot(new Slot(inventory, row, 10, 10 + row * 18));              // hotbar
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(inventory, 9 + row * 3 + col, 30 + col * 18, 10 + row * 18));
            }
        }
    }

    // Shift-click: inventory -> pot, pot -> inventory
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < POT_SLOTS) {
            if (!this.moveItemStackTo(stack, POT_SLOTS, this.slots.size(), false)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 0, POT_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    // Close the screen when you walk away or the pot is broken
    @Override
    public boolean stillValid(Player player) {
        return !pot.isRemoved()
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
