package net.skross.cozycookery.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import net.skross.cozycookery.client.CloseUpCamera;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.StationMenu;
import org.lwjgl.glfw.GLFW;

/**
 * Shared base for the station close-up screens: camera above the station, no blur, inventory panel on the left,
 * station panel on the right, a hint line at the bottom. Subclasses add the tool handling.
 */
public abstract class StationScreen<M extends StationMenu> extends AbstractContainerScreen<M> {
    private static final int PANEL = 0xB0261A12;
    private static final int PANEL_EDGE = 0xFF6E4A2A;
    private static final int SLOT = 0x60000000;
    private static final int STATION_SLOT = 0x40FFFFFF;

    protected StationScreen(M menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = StationMenu.WIDTH;
        this.imageHeight = StationMenu.HEIGHT;
    }

    /** Height of the working surface above the bottom of the block, in blocks (camera looks at this). */
    protected abstract double surfaceHeight();

    /** Lang key of the title of the right-hand panel ("In the pan"). */
    protected abstract String panelTitleKey();

    /** Lang key of the hint at the bottom. */
    protected abstract String hintKey();

    protected int hintColour() {
        return 0xFFFFFFFF;
    }

    /** True while the player is busy with the tool (no tooltips then). */
    protected boolean busy() {
        return false;
    }

    protected void onCloseUpStart() {
    }

    protected void onCloseUpEnd() {
    }

    protected void tickTool(int mouseX, int mouseY) {
    }

    protected void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    protected boolean toolMode() {
        return menu.mode == CloseUpMode.TOOL;
    }

    // ---------- Camera ----------
    @Override
    protected void init() {
        super.init();
        startCamera();
        onCloseUpStart();
    }

    @Override
    public void removed() {
        super.removed();
        onCloseUpEnd();
        setCursorHidden(false);
        CloseUpCamera.stop();
    }

    // Default: from your side, looking down at the working surface
    protected void startCamera() {
        CloseUpCamera.start(menu.pos, surfaceHeight());
    }

    protected static void setCursorHidden(boolean hidden) {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR,
                hidden ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL);
    }

    protected Vec3 surfaceCentre() {
        return new Vec3(menu.pos.getX() + 0.5, menu.pos.getY() + surfaceHeight(), menu.pos.getZ() + 0.5);
    }

    protected Vec3 mouseOnSurface(double mouseX, double mouseY) {
        return CloseUpCamera.mouseOnPlane(mouseX, mouseY, this.width, this.height, surfaceCentre().y);
    }

    protected double[] toScreen(Vec3 point) {
        return CloseUpCamera.toScreen(point, this.width, this.height);
    }

    protected void drawGrabFrame(GuiGraphics graphics, double[] p) {
        int x = (int) p[0];
        int y = (int) p[1];
        graphics.fill(x - 5, y - 5, x + 5, y - 4, 0xC0FFFFFF);
        graphics.fill(x - 5, y + 4, x + 5, y + 5, 0xC0FFFFFF);
        graphics.fill(x - 5, y - 4, x - 4, y + 4, 0xC0FFFFFF);
        graphics.fill(x + 4, y - 4, x + 5, y + 4, 0xC0FFFFFF);
    }

    // ---------- Drawing ----------
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        tickTool(mouseX, mouseY);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!busy()) {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
        drawOverlay(graphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBg(graphics, partialTick, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x + 4, y + 4, x + 88, y + 176, PANEL_EDGE);
        graphics.fill(x + 5, y + 5, x + 87, y + 175, PANEL);
        int rows = (menu.stationSlotCount() + 3) / 4;
        int px = x + StationMenu.PANEL_X;
        int py = y + StationMenu.PANEL_Y;
        graphics.fill(px, py, px + 84, py + 20 + rows * 18, PANEL_EDGE);
        graphics.fill(px + 1, py + 1, px + 83, py + 19 + rows * 18, PANEL);
        for (Slot slot : this.menu.slots) {
            int colour = slot.index < menu.stationSlotCount() ? STATION_SLOT : SLOT;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, colour);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, Component.translatable(panelTitleKey()),
                StationMenu.PANEL_X + 6, StationMenu.PANEL_Y + 5, 0xFFE8D2B0, false);
        Component hint = Component.translatable(hintKey());
        graphics.drawString(this.font, hint, (StationMenu.WIDTH - this.font.width(hint)) / 2,
                StationMenu.HEIGHT - 12, hintColour(), true);
    }
}
