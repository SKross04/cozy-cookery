package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.entity.PlateBlockEntity;

// The parts lying on an empty plate, one per quarter
public class PlateBlockEntityRenderer implements BlockEntityRenderer<PlateBlockEntity> {
    private static final float[][] SPOTS = {{6f, 6f}, {10f, 6f}, {6f, 10f}, {10f, 10f}};

    public PlateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PlateBlockEntity plate, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
        for (int i = 0; i < PlateBlockEntity.SLOTS; i++) {
            ItemStack part = plate.parts.getStackInSlot(i);
            if (part.isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(SPOTS[i][0] / 16f, 1.1f / 16f + i * 0.003f, SPOTS[i][1] / 16f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 90f + 30f));
            poseStack.scale(0.32f, 0.32f, 0.32f);
            Minecraft.getInstance().getItemRenderer().renderStatic(part, ItemDisplayContext.FIXED, packedLight,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, plate.getLevel(), i);
            poseStack.popPose();
        }
    }
}
