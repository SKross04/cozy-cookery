package net.skross.cozycookery.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.skross.cozycookery.block.entity.renderer.FryingPanBlockEntityRenderer;
import net.skross.cozycookery.client.ToolInStation;
import net.skross.cozycookery.menu.FryingPanMenu;
import net.skross.cozycookery.network.FlipPayload;
import org.lwjgl.glfw.GLFW;

/**
 * Frying pan close-up. Bare hand: drag food in and out. Spatula: grab the handle tip, move the spatula around the
 * pan and flick the mouse upwards to flip the food.
 */
public class FryingPanScreen extends StationScreen<FryingPanMenu> {
    private static final double GRAB_PIXELS = 9;
    private static final double PAN_RADIUS = 0.2;   // blocks: the spatula head stays inside the pan
    private static final double FLICK_PIXELS = 22;  // mouse must move up this far...
    private static final long FLICK_MILLIS = 150;   // ...within this time
    private static final long FLIP_COOLDOWN = 400;  // ms between two flips
    private boolean holding = false;
    private double flickStartY;
    private long flickStartTime;
    private long lastFlip = 0;

    public FryingPanScreen(FryingPanMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected double surfaceHeight() {
        return FryingPanBlockEntityRenderer.PAN_FLOOR + 0.02;
    }

    @Override
    protected String panelTitleKey() {
        return "closeup.cozycookery.frying_pan.panel";
    }

    @Override
    protected String hintKey() {
        if (!toolMode()) {
            return "closeup.cozycookery.frying_pan.add";
        }
        if (menu.pan.anyNeedsFlip()) {
            return "closeup.cozycookery.frying_pan.flip_now";
        }
        return holding ? "closeup.cozycookery.frying_pan.holding" : "closeup.cozycookery.frying_pan.tool";
    }

    @Override
    protected int hintColour() {
        return toolMode() && menu.pan.anyNeedsFlip() ? 0xFFFF5040 : 0xFFFFFFFF;
    }

    @Override
    protected boolean busy() {
        return holding;
    }

    @Override
    protected void onCloseUpStart() {
        if (toolMode()) {
            menu.pan.clientSpatulaIn = true;
        }
    }

    @Override
    protected void onCloseUpEnd() {
        menu.pan.clientSpatulaIn = false;
    }

    private Vec3 tip() {
        return ToolInStation.tip(menu.pos, 0.5 + menu.pan.clientSpatulaX, surfaceHeight(), 0.5 + menu.pan.clientSpatulaZ,
                FryingPanBlockEntityRenderer.SPATULA_TILT, FryingPanBlockEntityRenderer.SPATULA_SCALE);
    }

    private boolean overTip(double mouseX, double mouseY) {
        if (!toolMode()) {
            return false;
        }
        double[] t = toScreen(tip());
        return Math.hypot(mouseX - t[0], mouseY - t[1]) <= GRAB_PIXELS;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && menu.getCarried().isEmpty() && overTip(mouseX, mouseY)) {
            holding = true;
            flickStartY = mouseY;
            flickStartTime = System.currentTimeMillis();
            setCursorHidden(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (holding && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            holding = false;
            setCursorHidden(false);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void tickTool(int mouseX, int mouseY) {
        if (!holding) {
            return;
        }
        // the spatula head follows the mouse inside the pan
        Vec3 c = surfaceCentre();
        Vec3 m = mouseOnSurface(mouseX, mouseY);
        double dx = m.x - c.x;
        double dz = m.z - c.z;
        double r = Math.hypot(dx, dz);
        if (r > PAN_RADIUS) {
            dx *= PAN_RADIUS / r;
            dz *= PAN_RADIUS / r;
        }
        menu.pan.clientSpatulaX = (float) dx;
        menu.pan.clientSpatulaZ = (float) dz;

        // flick: the mouse moves up quickly
        long now = System.currentTimeMillis();
        if (mouseY > flickStartY || now - flickStartTime > FLICK_MILLIS) {
            flickStartY = mouseY;
            flickStartTime = now;
        } else if (flickStartY - mouseY >= FLICK_PIXELS && now - lastFlip > FLIP_COOLDOWN) {
            lastFlip = now;
            flickStartY = mouseY;
            flickStartTime = now;
            if (Minecraft.getInstance().level != null) {
                menu.pan.clientFlipStart = Minecraft.getInstance().level.getGameTime();
            }
            PacketDistributor.sendToServer(new FlipPayload(menu.pos));
        }
    }

    @Override
    protected void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!holding && overTip(mouseX, mouseY)) {
            drawGrabFrame(graphics, toScreen(tip()));
        }
    }
}
