package net.skross.cozycookery.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.entity.FryingPanBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

// Frying pan close-up: the 4 spots in the pan + your inventory
public class FryingPanMenu extends StationMenu {
    public final FryingPanBlockEntity pan;

    public FryingPanMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (FryingPanBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()),
                extraData.readEnum(CloseUpMode.class), ToolAccess.read(extraData));
    }

    public FryingPanMenu(int containerId, Inventory inventory, FryingPanBlockEntity pan, CloseUpMode mode,
                         ToolAccess tool) {
        super(ModMenuTypes.FRYING_PAN_MENU.get(), containerId, pan, mode, tool);
        this.pan = pan;
        for (int i = 0; i < FryingPanBlockEntity.SLOTS; i++) {
            final int index = i;
            addStationSlot(new SlotItemHandler(pan.items, i, slotX(i), slotY(i)) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return FryingPanBlockEntity.isFryable(inventory.player.level(), stack);
                }

                // a new piece starts with fresh timers
                @Override
                public void set(ItemStack stack) {
                    FryingPanMenu.this.pan.resetSlot(index);
                    super.set(stack);
                }

                @Override
                public void onTake(Player player, ItemStack stack) {
                    FryingPanMenu.this.pan.resetSlot(index);
                    super.onTake(player, stack);
                }
            });
        }
        addPlayerInventory(inventory);
    }
}
