package net.skross.cozycookery.client;

import net.minecraft.client.Minecraft;
import net.skross.cozycookery.screen.CookbookScreen;

// Client-only actions called from common code (only ever reached when level.isClientSide() is true)
public final class ClientHooks {
    private ClientHooks() {
    }

    public static void openCookbook() {
        Minecraft.getInstance().setScreen(new CookbookScreen());
    }
}
