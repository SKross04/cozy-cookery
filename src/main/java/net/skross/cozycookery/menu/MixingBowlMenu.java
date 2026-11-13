package net.skross.cozycookery.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.skross.cozycookery.block.entity.MixingBowlBlockEntity;
import net.skross.cozycookery.util.ToolAccess;

// Mixing bowl close-up: the 8 spots in the bowl + your inventory
public class MixingBowlMenu extends StationMenu {
    public final MixingBowlBlockEntity bowl;

    public MixingBowlMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, inventory,
                (MixingBowlBlockEntity) inventory.player.level().getBlockEntity(extraData.readBlockPos()),
                extraData.readEnum(CloseUpMode.class), ToolAccess.read(extraData));
    }

    public MixingBowlMenu(int containerId, Inventory inventory, MixingBowlBlockEntity bowl, CloseUpMode mode,
                          ToolAccess tool) {
        super(ModMenuTypes.MIXING_BOWL_MENU.get(), containerId, bowl, mode, tool);
        this.bowl = bowl;
        Player player = inventory.player;
        for (int i = 0; i < MixingBowlBlockEntity.SLOTS; i++) {
            addStationSlot(new SlotItemHandler(bowl.items, i, slotX(i), slotY(i)) {
                // Tools and empty buckets are no ingredients (milk buckets, water bottles and empty bowls are!)
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return !MixingBowlMenu.this.bowl.isMixed() && !MixingBowlMenu.this.bowl.hasSoup()
                            && !stack.isDamageableItem() && !stack.is(Items.BUCKET);
                }

                @Override
                public boolean mayPickup(Player p) {
                    return !MixingBowlMenu.this.bowl.hasSoup();
                }

                // Containers come back: milk bucket -> bucket, water bottle -> glass bottle
                @Override
                public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
                    super.setByPlayer(newStack, oldStack);
                    if (!player.level().isClientSide() && !newStack.isEmpty()) {
                        ItemStack container = newStack.is(Items.POTION) ? new ItemStack(Items.GLASS_BOTTLE)
                                : newStack.getCraftingRemainingItem();
                        if (!container.isEmpty()) {
                            player.getInventory().placeItemBackInInventory(container);
                        }
                    }
                }
            });
        }
        addPlayerInventory(inventory);
    }
}
