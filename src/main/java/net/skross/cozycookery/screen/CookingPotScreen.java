package net.skross.cozycookery.screen;

import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.skross.cozycookery.client.CloseUpCamera;
import net.skross.cozycookery.menu.CloseUpMode;
import net.skross.cozycookery.menu.CookingPotMenu;
import net.skross.cozycookery.network.StirPayload;
import org.lwjgl.glfw.GLFW;

/**
 * The pot close-up: no grey background and no menu frame. You look down into the real pot.
 * - Bare hand (ADD mode): your inventory sits in a panel on the left, the pot's ingredients float in a ring.
 * - Spoon (TOOL mode): the cursor becomes the spoon. Hold the left mouse button and stir in circles around the pot.
 */
public class CookingPotScreen extends AbstractContainerScreen<CookingPotMenu> {
    private static final int PANEL = 0xB0261A12;       // dark, see-through wood
    private static final int PANEL_EDGE = 0xFF6E4A2A;
    private static final int SLOT = 0x60000000;
    private static final int POT_SLOT = 0x40FFFFFF;

    // Stirring
    private static final float QUARTER = Mth.HALF_PI;
    private static final double MIN_RADIUS = 8;       // too close to the middle doesn't count
    private static final double MAX_RADIUS = 110;     // too far away doesn't count
    private static final float FAST = 0.9f;          // radians per frame: faster than this splashes
    private boolean stirring = false;
    private float lastAngle = 0;
    private float sinceQuarter = 0;  // radians stirred since the last message to the server
    private int quarters = 0;        // quarters since the last full circle
    private float spoonSpin = 0;     // how the spoon icon is turned

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
            setCursorHidden(true);
        }
    }

    @Override
    public void removed() {
        super.removed();
        setCursorHidden(false);
        CloseUpCamera.stop();
    }

    private static void setCursorHidden(boolean hidden) {
        GLFW.glfwSetInputMode(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_CURSOR,
                hidden ? GLFW.GLFW_CURSOR_HIDDEN : GLFW.GLFW_CURSOR_NORMAL);
    }

    // ---------- Stirring ----------
    private double potX() {
        return this.leftPos + CookingPotMenu.POT_CENTER_X;
    }

    private double potY() {
        return this.topPos + CookingPotMenu.POT_CENTER_Y;
    }

    private float angleTo(double mouseX, double mouseY) {
        return (float) Mth.atan2(mouseY - potY(), mouseX - potX());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (toolMode()) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                stirring = true;
                lastAngle = angleTo(mouseX, mouseY);
            }
            return true; // with the spoon you can't grab items
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (toolMode()) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                stirring = false;
            }
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    // Follows the mouse every frame and counts how far it went around the pot
    private void updateStir(int mouseX, int mouseY) {
        double dist = Math.hypot(mouseX - potX(), mouseY - potY());
        float angle = angleTo(mouseX, mouseY);
        if (!stirring || dist < MIN_RADIUS || dist > MAX_RADIUS) {
            lastAngle = angle;
            return;
        }
        float delta = Mth.wrapDegrees((angle - lastAngle) * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD;
        lastAngle = angle;
        spoonSpin += delta;
        sinceQuarter += Math.abs(delta);
        if (Math.abs(delta) > FAST) {
            splash();
        }
        while (sinceQuarter >= QUARTER) {
            sinceQuarter -= QUARTER;
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
        if (toolMode()) {
            drawSpoon(graphics, mouseX, mouseY);
        } else {
            this.renderTooltip(graphics, mouseX, mouseY);
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
        if (toolMode()) {
            // a faint dotted circle that shows where to stir
            for (int i = 0; i < 24; i++) {
                double a = i * (Math.PI * 2 / 24);
                int px = (int) (potX() + Math.cos(a) * 40);
                int py = (int) (potY() + Math.sin(a) * 30);
                graphics.fill(px - 1, py - 1, px + 1, py + 1, 0x50FFFFFF);
            }
            return;
        }
        // inventory panel
        graphics.fill(x + 4, y + 4, x + 88, y + 176, PANEL_EDGE);
        graphics.fill(x + 5, y + 5, x + 87, y + 175, PANEL);
        // slot backgrounds
        for (Slot slot : this.menu.slots) {
            int colour = slot.index < 8 ? POT_SLOT : SLOT;
            graphics.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, colour);
        }
    }

    // The spoon follows the cursor and turns with your stirring
    private void drawSpoon(GuiGraphics graphics, int mouseX, int mouseY) {
        ItemStack spoon = Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.getMainHandItem() : ItemStack.EMPTY;
        if (spoon.isEmpty()) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(mouseX, mouseY, 300);
        graphics.pose().mulPose(Axis.ZP.rotation(spoonSpin * 0.5f));
        graphics.pose().scale(2.5f, 2.5f, 1f);
        // the spoon's bowl (top-right of the icon) sits on the cursor
        graphics.renderItem(spoon, -13, -3);
        graphics.pose().popPose();
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
        } else {
            key = "closeup.cozycookery.cooking_pot.tool";
        }
        Component hint = Component.translatable(key);
        graphics.drawString(this.font, hint, CookingPotMenu.POT_CENTER_X - this.font.width(hint) / 2,
                CookingPotMenu.HEIGHT - 12, colour, true);
    }
}
