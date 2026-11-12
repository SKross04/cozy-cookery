package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.custom.StoveBlock;
import net.skross.cozycookery.block.entity.StoveBlockEntity;

public class StoveBlockEntityRenderer implements BlockEntityRenderer<StoveBlockEntity> {
    public StoveBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(StoveBlockEntity stove, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean lit = stove.getBlockState().getValue(StoveBlock.LIT);
        // When the fire burns, the fuel glows (full block light)
        int light = lit ? LightTexture.pack(15, LightTexture.sky(packedLight)) : packedLight;
        float rotation = (stove.getBlockState().getValue(StoveBlock.FACING).toYRot() + 180f) % 360f;

        for (int i = 0; i < StoveBlockEntity.FUEL_SLOTS; i++) {
            ItemStack stack = stove.fuel.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            // A small pile in the firebox: 2 rows of 4, each piece a tiny bit higher than the one before,
            // so pieces that overlap never flicker (no "z-fighting")
            float x = (5.2f + (i % 4) * 1.9f) / 16f;
            float y = (2.6f + i * 0.08f) / 16f;
            float z = (1.4f + (i / 4) * 1.4f) / 16f;

            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));   // same rotation as the block model
            poseStack.translate(-0.5f, 0f, -0.5f);
            poseStack.translate(x, y, z);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));          // lie flat
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 47f));      // each piece a bit differently turned
            poseStack.scale(0.22f, 0.22f, 0.22f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, stove.getLevel(), i);
            poseStack.popPose();
        }
    }
}
