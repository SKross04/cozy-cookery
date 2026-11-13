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
import net.minecraft.util.Mth;
import net.skross.cozycookery.client.ToolInStation;
import net.skross.cozycookery.item.ModItems;
import net.skross.cozycookery.block.custom.FryingPanBlock;
import net.skross.cozycookery.block.entity.FryingPanBlockEntity;

public class FryingPanBlockEntityRenderer implements BlockEntityRenderer<FryingPanBlockEntity> {
    public static final float PAN_FLOOR = 1f / 16f;
    public static final float SPATULA_TILT = 55f;   // degrees from upright, leaning towards you
    public static final float SPATULA_SCALE = 0.42f;
    // 2 x 2 spots inside the pan (pan floor: x 4-12, z 4-12)
    private static final float[][] SPOTS = {{6f, 6f}, {10f, 6f}, {6f, 10f}, {10f, 10f}};

    public FryingPanBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FryingPanBlockEntity pan, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (pan.clientSpatulaIn && pan.getLevel() != null) {
            ToolInStation.render(poseStack, bufferSource, packedLight, pan.getLevel(),
                    new ItemStack(ModItems.WOODEN_SPATULA.get()), pan.getBlockPos(),
                    0.5 + pan.clientSpatulaX, PAN_FLOOR + 0.02, 0.5 + pan.clientSpatulaZ, SPATULA_TILT, SPATULA_SCALE);
        }
        // a flick with the spatula: the food jumps up and turns over (half a second)
        float flip = pan.getLevel() == null ? 1f
                : Math.min(1f, (pan.getLevel().getGameTime() - pan.clientFlipStart + partialTick) / 10f);
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
            if (flip < 1f) {
                poseStack.translate(0, Mth.sin(flip * Mth.PI) * 0.25f, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(flip * 180f));
            }
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
