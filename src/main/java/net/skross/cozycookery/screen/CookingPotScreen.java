package net.skross.cozycookery.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.skross.cozycookery.client.CloseUpCamera;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.CookingPotMenu;

/**
 * The pot close-up: no grey background and no menu frame. You look down into the real pot,
 * your inventory sits in a small panel on the left and the pot's ingredients float in a ring around it.
 */
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final int PANEL = 0xB0261A12;       // dark, see-through wood
    private static final int PANEL_EDGE = 0xFF6E4A2A;
    private static final int SLOT = 0x60000000;
    private static final int POT_SLOT = 0x40FFFFFF;

    public CookingPotScreen(CookingPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CookingPotMenu.WIDTH;
        this.imageHeight = CookingPotMenu.HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        // the soup surface is about 7 pixels above the bottom of the pot
        CloseUpCamera.start(menu.pos, 7.0 / 16.0);
    }

    @Override
    public void removed() {
        super.removed();
        CloseUpCamera.stop();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    // No blur and no dark overlay: the world (the pot) stays fully visible
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        // inventory panel
        graphics.fill(x + 4, y + 4, x + 88, y + 176, PANEL_EDGE);
        graphics.fill(x + 5, y + 5, x + 87, y + 175, PANEL);
        // slot backgrounds
        for (Slot slot : this.menu.slots) {
            int colour = slot.index < 8 ? POT_SLOT : SLOT;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, colour);
        }
    }

    // Instead of "Inventory"/title labels: one small hint at the bottom
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        Component hint = Component.translatable(menu.mode == CloseUpMode.TOOL
                ? "closeup.cozycookery.cooking_pot.tool" : "closeup.cozycookery.cooking_pot.add");
        graphics.drawString(this.font, hint, CookingPotMenu.POT_CENTER_X - this.font.width(hint) / 2,
                CookingPotMenu.HEIGHT - 12, 0xFFFFFFFF, true);
    }
}
