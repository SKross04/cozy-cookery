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
import net.skross.cozycookery.block.custom.OvenBlock;
import net.skross.cozycookery.block.entity.OvenBlockEntity;

public class OvenBlockEntityRenderer implements BlockEntityRenderer<OvenBlockEntity> {
    private static final float TRAY = 5f / 16f;   // top of the iron tray inside the baking chamber
    // 2 x 2 spots on the tray (chamber: x 3-13, z 0-9 in the model)
    private static final float[][] SPOTS = {{5.8f, 3f}, {10.2f, 3f}, {5.8f, 6.8f}, {10.2f, 6.8f}};

    public OvenBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OvenBlockEntity oven, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        boolean lit = oven.getBlockState().getValue(OvenBlock.LIT);
        int glow = lit ? LightTexture.pack(15, LightTexture.sky(packedLight)) : packedLight;
        float rotation = (oven.getBlockState().getValue(OvenBlock.FACING).toYRot() + 180f) % 360f;

        // Baking tray
        for (int i = 0; i < OvenBlockEntity.TRAY_SLOTS; i++) {
            ItemStack stack = oven.tray.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            boolean is3d = itemRenderer.getModel(stack, oven.getLevel(), null, 0).isGui3d();
            poseStack.pushPose();
            turnWithBlock(poseStack, rotation);
            if (is3d) {
                // cakes stand upright
                poseStack.translate(SPOTS[i][0] / 16f, TRAY + 0.1f, SPOTS[i][1] / 16f);
                poseStack.scale(0.4f, 0.4f, 0.4f);
            } else {
                poseStack.translate(SPOTS[i][0] / 16f, TRAY + 0.01f + i * 0.003f, SPOTS[i][1] / 16f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.scale(0.33f, 0.33f, 0.33f);
            }
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, glow, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, oven.getLevel(), i);
            poseStack.popPose();
        }

        // Fuel in the firebox (firebox: x 5-11, z 0-3 in the model)
        for (int i = 0; i < OvenBlockEntity.FUEL_SLOTS; i++) {
            ItemStack stack = oven.fuel.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            poseStack.pushPose();
            turnWithBlock(poseStack, rotation);
            poseStack.translate((6f + (i % 4) * 1.35f) / 16f, (1.6f + i * 0.08f) / 16f, (1.0f + (i / 4) * 1.1f) / 16f);
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(i * 47f));
            poseStack.scale(0.18f, 0.18f, 0.18f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, glow, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, oven.getLevel(), 10 + i);
            poseStack.popPose();
        }
    }

    // Same rotation as the block model (the model is made facing north)
    private static void turnWithBlock(PoseStack poseStack, float rotation) {
        poseStack.translate(0.5f, 0f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotation));
        poseStack.translate(-0.5f, 0f, -0.5f);
    }
}
