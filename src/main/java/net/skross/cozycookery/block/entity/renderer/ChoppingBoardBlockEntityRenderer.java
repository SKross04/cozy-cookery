package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.skross.cozycookery.block.custom.ChoppingBoardBlock;
import net.skross.cozycookery.block.entity.ChoppingBoardBlockEntity;

public class ChoppingBoardBlockEntityRenderer implements BlockEntityRenderer<ChoppingBoardBlockEntity> {
    private static final float BOARD_TOP = 1f / 16f; // the board is 1 pixel high

    public ChoppingBoardBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChoppingBoardBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack = blockEntity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Direction facing = blockEntity.getBlockState().getValue(ChoppingBoardBlock.FACING);
        boolean is3dItem = itemRenderer.getModel(stack, blockEntity.getLevel(), null, 0).isGui3d();

        // Same rotation as the board model in the blockstate JSON (north 0, east 90, south 180, west 270).
        // Blockstate rotations go clockwise, PoseStack rotations go counter-clockwise -> minus sign.
        float boardRotation = (facing.toYRot() + 180f) % 360f;

        poseStack.pushPose();
        if (is3dItem) {
            // Blocks (stone, a cake...) stand upright on the board at half size
            poseStack.translate(0.5f, BOARD_TOP + 0.125f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-boardRotation));
        } else {
            // Flat items (meat, knives...) lie flat on the board
            poseStack.translate(0.5f, BOARD_TOP + 0.02f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-boardRotation));
            poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            // Most item textures are drawn diagonally (knives, strips, drumsticks).
            // Turning them 45 degrees makes them lie along the long side of the board.
            poseStack.mulPose(Axis.ZP.rotationDegrees(-45f));
        }
        poseStack.scale(0.5f, 0.5f, 0.5f);

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, blockEntity.getLevel(), (int) blockEntity.getBlockPos().asLong());
        poseStack.popPose();
    }
}
