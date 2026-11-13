package net.skross.cozycookery.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.skross.cozycookery.block.entity.renderer.ChoppingBoardBlockEntityRenderer;
import net.skross.cozycookery.menu.ChoppingBoardMenu;
import net.skross.cozycookery.network.ChopPayload;
import org.lwjgl.glfw.GLFW;

/**
 * Chopping board close-up. Bare hand: put something on the board or take it off.
 * Knife: the knife follows your mouse over the board, every left click is one cut.
 */
public class ChoppingBoardScreen extends StationScreen<ChoppingBoardMenu> {
    private static final long CHOP_COOLDOWN = 220; // ms between two cuts
    private boolean cursorHidden = false;

    public ChoppingBoardScreen(ChoppingBoardMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected double surfaceHeight() {
        return ChoppingBoardBlockEntityRenderer.BOARD_TOP;
    }

    @Override
    protected String panelTitleKey() {
        return "closeup.cozycookery.chopping_board.panel";
    }

    @Override
    protected String hintKey() {
        if (!toolMode()) {
            return "closeup.cozycookery.chopping_board.add";
        }
        return menu.board.isEmpty() ? "closeup.cozycookery.chopping_board.empty"
                : "closeup.cozycookery.chopping_board.tool";
    }

    private ItemStack knife() {
        return Minecraft.getInstance().player == null ? ItemStack.EMPTY : menu.tool.get(Minecraft.getInstance().player);
    }

    @Override
    protected void onCloseUpStart() {
        if (toolMode()) {
            menu.board.clientKnife = knife().copy();
        }
    }

    @Override
    protected void onCloseUpEnd() {
        menu.board.clientKnife = ItemStack.EMPTY;
    }

    // The board is 16 x 10 pixels: is the mouse above it?
    private boolean overBoard(Vec3 point) {
        Vec3 c = surfaceCentre();
        return Math.abs(point.x - c.x) < 0.5 && Math.abs(point.z - c.z) < 0.5;
    }

    @Override
    protected void tickTool(int mouseX, int mouseY) {
        if (!toolMode()) {
            return;
        }
        menu.board.clientKnife = knife().copy(); // follows durability / a broken knife
        Vec3 m = mouseOnSurface(mouseX, mouseY);
        boolean over = overBoard(m) && !isOverPanels(mouseX, mouseY);
        if (over) {
            Vec3 c = surfaceCentre();
            menu.board.clientKnifeX = (float) Math.max(-0.4, Math.min(0.4, m.x - c.x));
            menu.board.clientKnifeZ = (float) Math.max(-0.4, Math.min(0.4, m.z - c.z));
        }
        if (over != cursorHidden) {
            cursorHidden = over;
            setCursorHidden(over);
        }
    }

    private boolean isOverPanels(double mouseX, double mouseY) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        boolean leftPanel = x >= 4 && x < 88 && y >= 4 && y < 176;
        boolean rightPanel = x >= ChoppingBoardMenu.PANEL_X && x < ChoppingBoardMenu.PANEL_X + 84
                && y >= ChoppingBoardMenu.PANEL_Y && y < ChoppingBoardMenu.PANEL_Y + 40;
        return leftPanel || rightPanel;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (toolMode() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && menu.getCarried().isEmpty()
                && !isOverPanels(mouseX, mouseY) && overBoard(mouseOnSurface(mouseX, mouseY))) {
            long now = System.currentTimeMillis();
            if (now - menu.board.clientChopStart > CHOP_COOLDOWN) {
                menu.board.clientChopStart = now;
                PacketDistributor.sendToServer(new ChopPayload(menu.pos));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
