package net.skross.cozycookery.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.skross.cozycookery.block.custom.OvenBlock;
import net.skross.cozycookery.client.CloseUpCamera;
import net.skross.cozycookery.menu.OvenMenu;
import net.skross.cozycookery.menu.StationMenu;

// Oven close-up: you crouch in front of the oven and look into the baking chamber
public class OvenScreen extends StationScreen<OvenMenu> {
    public OvenScreen(OvenMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected double surfaceHeight() {
        return 6.0 / 16.0; // just above the baking tray
    }

    @Override
    protected void startCamera() {
        CloseUpCamera.startFront(menu.pos, menu.oven.getBlockState().getValue(OvenBlock.FACING),
                surfaceHeight(), 1.15, 0.35);
    }

    @Override
    protected String panelTitleKey() {
        return "closeup.cozycookery.oven.panel";
    }

    @Override
    protected String hintKey() {
        return menu.oven.getBlockState().getValue(OvenBlock.LIT) ? "closeup.cozycookery.oven.lit"
                : "closeup.cozycookery.oven.unlit";
    }

    // small labels: "Tray" next to the top row, "Fuel" next to the rows below
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font, Component.translatable("closeup.cozycookery.oven.tray"),
                StationMenu.PANEL_X - 30, StationMenu.PANEL_Y + 20, 0xFFE8D2B0, true);
        graphics.drawString(this.font, Component.translatable("closeup.cozycookery.oven.fuel"),
                StationMenu.PANEL_X - 30, StationMenu.PANEL_Y + 47, 0xFFE8D2B0, true);
    }
}
