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
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.block.custom.PotLiquid;
import net.skross.cozycookery.block.entity.CookingPotBlockEntity;

public class CookingPotBlockEntityRenderer implements BlockEntityRenderer<CookingPotBlockEntity> {
    private static final float SURFACE = 7f / 16f; // the water surface in the pot model

    public CookingPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CookingPotBlockEntity pot, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (pot.getBlockState().getValue(CookingPotBlock.LIQUID) == PotLiquid.EMPTY || pot.getLevel() == null) {
            return;
        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        float time = pot.getLevel().getGameTime() + partialTick;

        for (int i = 0; i < CookingPotBlockEntity.INGREDIENT_SLOTS; i++) {
            ItemStack stack = pot.ingredients.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            // Ingredients float in a slow circle around the middle of the pot and bob up and down
            float angle = i * 45f + time * 0.6f;
            float radius = 2.4f / 16f;
            float x = 0.5f + Mth.cos(angle * Mth.DEG_TO_RAD) * radius;
            float z = 0.5f + Mth.sin(angle * Mth.DEG_TO_RAD) * radius;
            float bob = Mth.sin(time * 0.1f + i * 1.3f) * 0.006f;

            poseStack.pushPose();
            poseStack.translate(x, SURFACE + 0.01f + i * 0.003f + bob, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-angle));
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));   // lie flat on the water
            poseStack.scale(0.3f, 0.3f, 0.3f);
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                    poseStack, bufferSource, pot.getLevel(), i);
            poseStack.popPose();
        }
    }
}
