package net.skross.cozycookery.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.skross.cozycookery.menu.MortarMenu;
import net.skross.cozycookery.network.GrindPayload;
import org.lwjgl.glfw.GLFW;

/**
 * Mortar close-up: drag something in, then hold the left mouse button over the mortar and grind in circles.
 * Every full circle is one grind; after a few circles the item is ground (wheat -> flour).
 */
public class MortarScreen extends StationScreen<MortarMenu> {
    private static final double MORTAR_RADIUS = 0.45; // blocks: where you may start grinding
    private boolean grinding = false;
    private float lastAngle = 0;
    private float turned = 0; // radians since the last full circle

    public MortarScreen(MortarMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected double surfaceHeight() {
        return 1.0 / 16.0 + 0.02;
    }

    @Override
    protected String panelTitleKey() {
        return "closeup.cozycookery.mortar.panel";
    }

    @Override
    protected String hintKey() {
        if (menu.mortar.isEmpty()) {
            return "closeup.cozycookery.mortar.empty";
        }
        return grinding ? "closeup.cozycookery.mortar.grinding" : "closeup.cozycookery.mortar.tool";
    }

    @Override
    protected boolean busy() {
        return grinding;
    }

    private float angleOf(Vec3 point) {
        Vec3 c = surfaceCentre();
        return (float) Mth.atan2(point.z - c.z, point.x - c.x);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec3 m = mouseOnSurface(mouseX, mouseY);
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && menu.getCarried().isEmpty() && this.hoveredSlot == null
                && m.distanceTo(surfaceCentre()) < MORTAR_RADIUS) {
            grinding = true;
            lastAngle = angleOf(m);
            setCursorHidden(true);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (grinding && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            grinding = false;
            setCursorHidden(false);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    protected void tickTool(int mouseX, int mouseY) {
        if (!grinding) {
            return;
        }
        float angle = angleOf(mouseOnSurface(mouseX, mouseY));
        float delta = Mth.wrapDegrees((angle - lastAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        lastAngle = angle;
        menu.mortar.clientGrindSpin += delta * Mth.RAD_TO_DEG;
        turned += Math.abs(delta);
        if (turned >= Mth.TWO_PI) {
            turned -= Mth.TWO_PI;
            PacketDistributor.sendToServer(new GrindPayload(menu.pos));
        }
    }
}
