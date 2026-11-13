package net.skross.cozycookery.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.skross.cozycookery.block.entity.renderer.CookingPotBlockEntityRenderer;
import net.skross.cozycookery.client.CloseUpCamera;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.CookingPotMenu;
import net.skross.cozycookery.network.StirPayload;
import org.lwjgl.glfw.GLFW;

/**
 * The pot close-up: no grey background and no menu frame. You look down into the real pot.
 * - Bare hand (ADD mode): your inventory sits in a panel on the left, the pot's ingredients float in a ring.
 * - Spoon (TOOL mode): the same, plus the spoon stands in the pot. Grab it (left click + hold) and stir in
 *   circles: the spoon and the ingredients move with your mouse.
 */
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final int PANEL = 0xB0261A12;       // dark, see-through wood
    private static final int PANEL_EDGE = 0xFF6E4A2A;
    private static final int SLOT = 0x60000000;
    private static final int POT_SLOT = 0x40FFFFFF;

    // Stirring
    private static final double GRAB_PIXELS = 9;      // how close to the handle tip you must click (GUI pixels)
    private static final double POT_RADIUS = 0.27;    // blocks: the spoon's bowl stays inside the pot
    private static final double QUARTER_PATH = 0.25;  // blocks the spoon must travel for one quarter stir
    private static final double FAST_STEP = 0.12;     // blocks per frame: faster than this splashes
    private boolean stirring = false; // holding the spoon
    private double sinceQuarter = 0;  // distance stirred since the last message to the server
    private int quarters = 0;         // quarters since the last full circle

    public CookingPotScreen(CookingPotMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = CookingPotMenu.WIDTH;
        this.imageHeight = CookingPotMenu.HEIGHT;
    }

    private boolean toolMode() {
        return menu.mode == CloseUpMode.TOOL;
    }

    @Override
    protected void init() {
        super.init();
        // the soup surface is about 7 pixels above the bottom of the pot
        CloseUpCamera.start(menu.pos, 7.0 / 16.0);
        if (toolMode()) {
            menu.pot.clientSpoonIn = true;
        }
    }

    @Override
    public void removed() {
        super.removed();
        menu.pot.clientSpoonIn = false;
        setCursorHidden(false);
        CloseUpCamera.stop();
    }

    private static void setCursorHidden(boolean hidden) {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR,
                hidden ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL);
    }

    // ---------- Stirring ----------
    private Vec3 potCentre() {
        BlockPos pos = menu.pos;
        return new Vec3(pos.getX() + 0.5, pos.getY() + CookingPotBlockEntityRenderer.SURFACE, pos.getZ() + 0.5);
    }

    private double[] tipOnScreen() {
        return CloseUpCamera.toScreen(CookingPotBlockEntityRenderer.spoonTip(menu.pot), this.width, this.height);
    }

    private boolean overSpoonTip(double mouseX, double mouseY) {
        if (!toolMode()) {
            return false;
        }
        double[] tip = tipOnScreen();
        return Math.hypot(mouseX - tip[0], mouseY - tip[1]) <= GRAB_PIXELS;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && this.menu.getCarried().isEmpty() && overSpoonTip(mouseX, mouseY)) {
            stirring = true;
            setCursorHidden(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (stirring && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            stirring = false;
            setCursorHidden(false);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // While you hold the spoon its bowl follows the mouse anywhere in the pot. The ingredients swirl along with
    // the spoon's movement around the middle, and every bit of stirring is reported to the server.
    private void updateStir(int mouseX, int mouseY) {
        if (!stirring) {
            return;
        }
        Vec3 c = potCentre();
        Vec3 m = CloseUpCamera.mouseOnPlane(mouseX, mouseY, this.width, this.height, c.y);
        double dx = m.x - c.x;
        double dz = m.z - c.z;
        double r = Math.hypot(dx, dz);
        if (r > POT_RADIUS) {
            dx *= POT_RADIUS / r;
            dz *= POT_RADIUS / r;
        }
        double oldX = menu.pot.clientSpoonX;
        double oldZ = menu.pot.clientSpoonZ;
        double moved = Math.hypot(dx - oldX, dz - oldZ);
        if (Math.hypot(oldX, oldZ) > 0.04 && Math.hypot(dx, dz) > 0.04) {
            float delta = Mth.wrapDegrees((float) (Math.toDegrees(Math.atan2(dz, dx) - Math.atan2(oldZ, oldX))));
            menu.pot.clientSwirl += delta * 0.8f;
        }
        menu.pot.clientSpoonX = (float) dx;
        menu.pot.clientSpoonZ = (float) dz;
        if (moved > FAST_STEP) {
            splash();
        }
        sinceQuarter += moved;
        while (sinceQuarter >= QUARTER_PATH) {
            sinceQuarter -= QUARTER_PATH;
            quarters++;
            boolean fullTurn = quarters >= 4;
            if (fullTurn) {
                quarters = 0;
            }
            PacketDistributor.sendToServer(new StirPayload(menu.pos, fullTurn));
        }
    }

    // Stirring too wildly makes the soup splash over the rim
    private void splash() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        BlockPos pos = menu.pos;
        for (int i = 0; i < 3; i++) {
            mc.level.addParticle(ParticleTypes.SPLASH,
                    pos.getX() + 0.2 + mc.level.random.nextDouble() * 0.6, pos.getY() + 0.55,
                    pos.getZ() + 0.2 + mc.level.random.nextDouble() * 0.6, 0.0, 0.15, 0.0);
        }
    }

    // ---------- Drawing ----------
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (toolMode()) {
            updateStir(mouseX, mouseY);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
        if (!stirring) {
            this.renderTooltip(graphics, mouseX, mouseY);
        }
        if (!stirring && overSpoonTip(mouseX, mouseY)) {
            // a little frame around the handle tip: "you can grab this"
            double[] p = tipOnScreen();
            int x = (int) p[0];
            int y = (int) p[1];
            graphics.fill(x - 5, y - 5, x + 5, y - 4, 0xC0FFFFFF);
            graphics.fill(x - 5, y + 4, x + 5, y + 5, 0xC0FFFFFF);
            graphics.fill(x - 5, y - 4, x - 4, y + 4, 0xC0FFFFFF);
            graphics.fill(x + 4, y - 4, x + 5, y + 4, 0xC0FFFFFF);
        }
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
        // "In the pot" panel
        int px = x + CookingPotMenu.POT_PANEL_X;
        int py = y + CookingPotMenu.POT_PANEL_Y;
        graphics.fill(px, py, px + 84, py + 56, PANEL_EDGE);
        graphics.fill(px + 1, py + 1, px + 83, py + 55, PANEL);
        // slot backgrounds
        for (Slot slot : this.menu.slots) {
            int colour = slot.index < 8 ? POT_SLOT : SLOT;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, colour);
        }
    }

    // Instead of "Inventory"/title labels: one small hint at the bottom
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        String key;
        int colour = 0xFFFFFFFF;
        if (!toolMode()) {
            key = "closeup.cozycookery.cooking_pot.add";
        } else if (menu.pot.isSticking()) {
            key = "closeup.cozycookery.cooking_pot.sticking";
            colour = 0xFFFF5040;
        } else if (!menu.pot.isCooking()) {
            key = "closeup.cozycookery.cooking_pot.nothing";
        } else if (stirring) {
            key = "closeup.cozycookery.cooking_pot.stirring";
        } else {
            key = "closeup.cozycookery.cooking_pot.tool";
        }
        graphics.drawString(this.font, Component.translatable("closeup.cozycookery.cooking_pot.in_the_pot"),
                CookingPotMenu.POT_PANEL_X + 6, CookingPotMenu.POT_PANEL_Y + 5, 0xFFE8D2B0, false);
        Component hint = Component.translatable(key);
        graphics.drawString(this.font, hint, CookingPotMenu.POT_CENTER_X - this.font.width(hint) / 2,
                CookingPotMenu.HEIGHT - 12, colour, true);
    }
}
