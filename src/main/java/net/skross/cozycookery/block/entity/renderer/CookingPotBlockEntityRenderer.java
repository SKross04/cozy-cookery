package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.custom.CookingPotBlock;
import net.skross.cozycookery.block.custom.PotLiquid;
import net.skross.cozycookery.block.entity.CookingPotBlockEntity;
import net.skross.cozycookery.item.ModItems;

public class CookingPotBlockEntityRenderer implements BlockEntityRenderer<CookingPotBlockEntity> {
    public static final float SURFACE = 7f / 16f; // the water surface in the pot model

    public CookingPotBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CookingPotBlockEntity pot, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (pot.getLevel() == null) {
            return;
        }
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        if (pot.clientSpoonIn) {
            renderSpoon(pot, poseStack, bufferSource, packedLight, itemRenderer);
        }
        if (pot.getBlockState().getValue(CookingPotBlock.LIQUID) == PotLiquid.EMPTY) {
            return;
        }
        float time = pot.getLevel().getGameTime() + partialTick;

        for (int i = 0; i < CookingPotBlockEntity.INGREDIENT_SLOTS; i++) {
            ItemStack stack = pot.ingredients.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }
            // Ingredients float in a slow circle around the middle of the pot and bob up and down
            float angle = i * 45f + time * 0.6f + pot.clientSwirl; // stirring pushes them around
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

    // ---------- The wooden spoon standing in the pot ----------
    public static final float SPOON_SCALE = 0.45f;
    private static final float SPOON_TILT = 25f;   // degrees the handle leans towards you
    private static final float BOWL_OFFSET = 0.46f; // icon units from the icon's middle to the spoon's bowl
    private static final float TIP_OFFSET = 0.60f;  // icon units from the icon's middle to the handle's tip

    // Horizontal direction from the pot to the camera, so the handle always leans towards you
    private static Vec3 towardsCamera(BlockPos pos) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 h = new Vec3(cam.x - (pos.getX() + 0.5), 0, cam.z - (pos.getZ() + 0.5));
        return h.lengthSqr() < 1.0E-6 ? new Vec3(0, 0, 1) : h.normalize();
    }

    /** World position of the tip of the spoon handle: that's where you grab it. */
    public static Vec3 spoonTip(CookingPotBlockEntity pot) {
        BlockPos pos = pot.getBlockPos();
        Vec3 h = towardsCamera(pos);
        double t = Math.toRadians(SPOON_TILT);
        double length = (BOWL_OFFSET + TIP_OFFSET) * SPOON_SCALE;
        return new Vec3(pos.getX() + 0.5 + pot.clientSpoonX + h.x * Math.sin(t) * length,
                pos.getY() + SURFACE + Math.cos(t) * length,
                pos.getZ() + 0.5 + pot.clientSpoonZ + h.z * Math.sin(t) * length);
    }

    private void renderSpoon(CookingPotBlockEntity pot, PoseStack poseStack, MultiBufferSource bufferSource,
                             int packedLight, ItemRenderer itemRenderer) {
        Vec3 h = towardsCamera(pot.getBlockPos());
        poseStack.pushPose();
        poseStack.translate(0.5f + pot.clientSpoonX, SURFACE, 0.5f + pot.clientSpoonZ); // the bowl, in the soup
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(h.x, h.z))); // face the camera
        poseStack.mulPose(Axis.XP.rotationDegrees(SPOON_TILT));            // lean the handle towards you
        poseStack.scale(SPOON_SCALE, SPOON_SCALE, SPOON_SCALE);
        poseStack.translate(0, BOWL_OFFSET, 0);                           // bowl at the bottom
        poseStack.mulPose(Axis.ZP.rotationDegrees(135f));                  // icon diagonal -> handle straight up
        itemRenderer.renderStatic(new ItemStack(ModItems.WOODEN_SPOON.get()), ItemDisplayContext.FIXED, packedLight,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, pot.getLevel(), 99);
        poseStack.popPose();
    }
}
