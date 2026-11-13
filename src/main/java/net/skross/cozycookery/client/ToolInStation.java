package net.skross.cozycookery.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Draws a tool (spatula, knife...) standing in a station during a close-up, and tells where its handle tip is.
 * The tool's working end (blade, spatula head) sits at (x, y, z) inside the block; the handle points up and leans
 * `tilt` degrees towards the camera. Item icons are drawn diagonally (working end top-right), which is why the
 * icon is turned 135 degrees.
 */
public final class ToolInStation {
    private static final float HEAD_OFFSET = 0.46f; // icon units from the icon middle to the working end
    private static final float TIP_OFFSET = 0.60f;  // icon units from the icon middle to the handle tip

    private ToolInStation() {
    }

    private static Vec3 towardsCamera(BlockPos pos) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 h = new Vec3(cam.x - (pos.getX() + 0.5), 0, cam.z - (pos.getZ() + 0.5));
        return h.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : h.normalize();
    }

    public static void render(PoseStack poseStack, MultiBufferSource buffers, int light, Level level, ItemStack tool,
                              BlockPos pos, double x, double y, double z, float tilt, float scale) {
        Vec3 h = towardsCamera(pos);
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(h.x, h.z)));
        poseStack.mulPose(Axis.XP.rotationDegrees(tilt));
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, HEAD_OFFSET, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(135f));
        Minecraft.getInstance().getItemRenderer().renderStatic(tool, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 98);
        poseStack.popPose();
    }

    /** World position of the handle tip (x, y, z are inside the block, like in render). */
    public static Vec3 tip(BlockPos pos, double x, double y, double z, float tilt, float scale) {
        Vec3 h = towardsCamera(pos);
        double t = Math.toRadians(tilt);
        double length = (HEAD_OFFSET + TIP_OFFSET) * scale;
        return new Vec3(pos.getX() + x + h.x * Math.sin(t) * length,
                pos.getY() + y + Math.cos(t) * length,
                pos.getZ() + z + h.z * Math.sin(t) * length);
    }
}
