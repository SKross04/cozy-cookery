package net.skross.cozycookery.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.skross.cozycookery.menu.PlateMenu;

// Plating close-up: drag the parts of a meal onto the plate; when they match a meal, the plate becomes it
public class PlateScreen extends StationScreen<PlateMenu> {
    public PlateScreen(PlateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected double surfaceHeight() {
        return 1.0 / 16.0;
    }

    @Override
    protected String panelTitleKey() {
        return "closeup.cozycookery.plate.panel";
    }

    @Override
    protected String hintKey() {
        return "closeup.cozycookery.plate.hint";
    }
}
