package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.custom.ToolRackBlock;
import net.skross.cozycookery.block.entity.ToolRackBlockEntity;

// Draws the tools hanging from the hooks: handle up, blade/bowl down, flat against the wall
public class ToolRackBlockEntityRenderer implements BlockEntityRenderer<ToolRackBlockEntity> {
    private static final float[] HOOK_X = {3f, 6.33f, 9.67f, 13f}; // pixels, left to right

    public ToolRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ToolRackBlockEntity rack, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        Direction facing = rack.getBlockState().getValue(ToolRackBlock.FACING);
        for (int i = 0; i < ToolRackBlockEntity.SLOTS; i++) {
            ItemStack tool = rack.getTool(i);
            if (tool.isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot())); // local +Z = out of the wall
            poseStack.translate((HOOK_X[i] - 8f) / 16f, 0.42f, -0.5f + 3.6f / 16f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(135f)); // icon diagonal -> handle up
            poseStack.scale(0.42f, 0.42f, 0.42f);
            Minecraft.getInstance().getItemRenderer().renderStatic(tool, ItemDisplayContext.FIXED, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, rack.getLevel(), i);
            poseStack.popPose();
        }
    }
}
