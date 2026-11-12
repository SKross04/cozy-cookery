package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.custom.FryingPanBlock;
import net.skross.cozycookery.block.entity.FryingPanBlockEntity;

public class FryingPanBlockEntityRenderer implements BlockEntityRenderer<FryingPanBlockEntity> {
    private static final float PAN_FLOOR = 1f / 16f;
    // 2 x 2 spots inside the pan (pan floor: x 4-12, z 4-12)
    private static final float[][] SPOTS = {{6f, 6f}, {10f, 6f}, {6f, 10f}, {10f, 10f}};

    public FryingPanBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FryingPanBlockEntity pan, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float rotation = (pan.getBlockState().getValue(FryingPanBlock.FACING).toYRot() + 180f) % 360f;

        for (int i = 0; i < FryingPanBlockEntity.SLOTS; i++) {
            ItemStack stack = pan.items.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));      // same rotation as the block model
            poseStack.translate(-0.5f, 0f, -0.5f);
            poseStack.translate(SPOTS[i][0] / 16f, PAN_FLOOR + 0.015f + i * 0.003f, SPOTS[i][1] / 16f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));             // lie flat
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 90f + 20f));   // each piece turned a bit differently
            if (pan.isFlipped(i)) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));        // flipped: the other side is up
            }
            poseStack.scale(0.36f, 0.36f, 0.36f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, pan.getLevel(), i);
            poseStack.popPose();
        }
    }
}
