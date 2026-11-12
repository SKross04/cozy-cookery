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
            // 2 rows of 4 inside the firebox (firebox: x 4-12, y 2, z 0-4 in the model)
            float x = (5f + (i % 4) * 2f) / 16f;
            float z = (1.3f + (i / 4) * 1.5f) / 16f;

            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));   // same rotation as the block model
            poseStack.translate(-0.5f, 0f, -0.5f);
            poseStack.translate(x, 2.2f / 16f, z);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));          // lie flat
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 47f));      // each piece a bit differently turned
            poseStack.scale(0.28f, 0.28f, 0.28f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, stove.getLevel(), i);
            poseStack.popPose();
        }
    }
}
