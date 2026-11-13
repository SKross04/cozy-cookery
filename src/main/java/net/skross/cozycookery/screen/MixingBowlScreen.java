package net.skross.cozycookery.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.skross.cozycookery.block.entity.renderer.MixingBowlBlockEntityRenderer;
import net.skross.cozycookery.client.ToolInStation;
import net.skross.cozycookery.menu.MixingBowlMenu;
import net.skross.cozycookery.network.MixPayload;
import org.lwjgl.glfw.GLFW;

/**
 * Mixing bowl close-up. Bare hand: drag ingredients in and out. Spoon: it stands in the bowl; grab the handle tip
 * and mix anywhere in the bowl until the ingredients turn into dough or batter.
 */
public class MixingBowlScreen extends StationScreen<MixingBowlMenu> {
    private static final double GRAB_PIXELS = 9;
    private static final double BOWL_RADIUS = 0.22;
    private static final double QUARTER_PATH = 0.22;
    private boolean holding = false;
    private double sinceQuarter = 0;
    private int quarters = 0;

    public MixingBowlScreen(MixingBowlMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected double surfaceHeight() {
        return MixingBowlBlockEntityRenderer.BOWL_FLOOR + 0.03;
    }

    @Override
    protected String panelTitleKey() {
        return "closeup.cozycookery.mixing_bowl.panel";
    }

    @Override
    protected String hintKey() {
        if (!toolMode()) {
            return "closeup.cozycookery.mixing_bowl.add";
        }
        if (menu.bowl.isMixed()) {
            return "closeup.cozycookery.mixing_bowl.done";
        }
        return holding ? "closeup.cozycookery.mixing_bowl.holding" : "closeup.cozycookery.mixing_bowl.tool";
    }

    @Override
    protected boolean busy() {
        return holding;
    }

    @Override
    protected void onCloseUpStart() {
        if (toolMode()) {
            menu.bowl.clientSpoonIn = true;
        }
    }

    @Override
    protected void onCloseUpEnd() {
        menu.bowl.clientSpoonIn = false;
    }

    private Vec3 tip() {
        return ToolInStation.tip(menu.pos, 0.5 + menu.bowl.clientSpoonX, surfaceHeight(), 0.5 + menu.bowl.clientSpoonZ,
                0f, MixingBowlBlockEntityRenderer.SPOON_SCALE);
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
        Vec3 c = surfaceCentre();
        Vec3 m = mouseOnSurface(mouseX, mouseY);
        double dx = m.x - c.x;
        double dz = m.z - c.z;
        double r = Math.hypot(dx, dz);
        if (r > BOWL_RADIUS) {
            dx *= BOWL_RADIUS / r;
            dz *= BOWL_RADIUS / r;
        }
        double oldX = menu.bowl.clientSpoonX;
        double oldZ = menu.bowl.clientSpoonZ;
        if (Math.hypot(oldX, oldZ) > 0.03 && Math.hypot(dx, dz) > 0.03) {
            menu.bowl.clientSwirl += 0.8f * Mth.wrapDegrees((float) Math.toDegrees(Math.atan2(dz, dx) - Math.atan2(oldZ, oldX)));
        }
        sinceQuarter += Math.hypot(dx - oldX, dz - oldZ);
        menu.bowl.clientSpoonX = (float) dx;
        menu.bowl.clientSpoonZ = (float) dz;
        while (sinceQuarter >= QUARTER_PATH) {
            sinceQuarter -= QUARTER_PATH;
            quarters++;
            boolean fullTurn = quarters >= 4;
            if (fullTurn) {
                quarters = 0;
            }
            PacketDistributor.sendToServer(new MixPayload(menu.pos, fullTurn));
        }
    }

    @Override
    protected void drawOverlay(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!holding && overTip(mouseX, mouseY)) {
            drawGrabFrame(graphics, toScreen(tip()));
        }
    }
}
