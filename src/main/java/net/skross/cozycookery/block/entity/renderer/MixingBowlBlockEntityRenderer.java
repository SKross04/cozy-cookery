package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.entity.MixingBowlBlockEntity;
import net.skross.cozycookery.client.ToolInStation;
import net.skross.cozycookery.item.ModItems;

public class MixingBowlBlockEntityRenderer implements BlockEntityRenderer<MixingBowlBlockEntity> {
    public static final float BOWL_FLOOR = 1f / 16f;
    public static final float SPOON_SCALE = 0.42f;

    public MixingBowlBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MixingBowlBlockEntity bowl, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (bowl.clientSpoonIn && bowl.getLevel() != null) {
            ToolInStation.render(poseStack, bufferSource, packedLight, bowl.getLevel(),
                    new ItemStack(ModItems.WOODEN_SPOON.get()), bowl.getBlockPos(),
                    0.5 + bowl.clientSpoonX, BOWL_FLOOR + 0.03, 0.5 + bowl.clientSpoonZ, 0f, SPOON_SCALE);
        }

        // Finished dough: one big item in the middle
        if (bowl.isMixed()) {
            ItemStack dough = bowl.items.getStackInSlot(0);
            if (!dough.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(0.5f, BOWL_FLOOR + 0.02f, 0.5f);
                poseStack.mulPose(Axis.XP.rotationDegrees(90f));
                poseStack.scale(0.5f, 0.5f, 0.5f);
                itemRenderer.renderStatic(dough, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                        poseStack, bufferSource, bowl.getLevel(), 0);
                poseStack.popPose();
            }
            return;
        }

        // Ingredients: lying in a circle on the bottom of the bowl
        for (int i = 0; i < MixingBowlBlockEntity.SLOTS; i++) {
            ItemStack stack = bowl.items.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            float angle = i * 45f + bowl.clientSwirl; // stirring pushes them around
            float x = 0.5f + Mth.cos(angle * Mth.DEG_TO_RAD) * (2.4f / 16f);
            float z = 0.5f + Mth.sin(angle * Mth.DEG_TO_RAD) * (2.4f / 16f);
            poseStack.pushPose();
            poseStack.translate(x, BOWL_FLOOR + 0.015f + i * 0.004f, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            poseStack.scale(0.3f, 0.3f, 0.3f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, bowl.getLevel(), i);
            poseStack.popPose();
        }
    }
}
