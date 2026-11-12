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
import net.skross.cozycookery.block.entity.MortarBlockEntity;

public class MortarBlockEntityRenderer implements BlockEntityRenderer<MortarBlockEntity> {
    public MortarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MortarBlockEntity mortar, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = mortar.getItem();
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.5f, 1f / 16f + 0.02f, 0.5f);   // on the bottom of the bowl
        poseStack.mulPose(Axis.XP.rotationDegrees(90f));     // lie flat
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45f));    // diagonal items fit better in the bowl
        poseStack.scale(0.35f, 0.35f, 0.35f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, mortar.getLevel(), 0);
        poseStack.popPose();
    }
}
