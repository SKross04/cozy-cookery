package net.skross.cozycookery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
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
    public static final float BOARD_TOP = 2f / 16f; // the board is 2 pixels high
    // The cutting surface is 1 pixel left of the block centre (the handle sticks out on the right)
    private static final float SURFACE_OFFSET = -1f / 16f;

    public ChoppingBoardBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChoppingBoardBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!blockEntity.clientKnife.isEmpty() && blockEntity.getLevel() != null) {
            renderKnife(blockEntity, poseStack, bufferSource, packedLight);
        }
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
            poseStack.translate(SURFACE_OFFSET, 0f, 0f);
        } else {
            // Flat items (meat, knives...) lie flat on the board
            poseStack.translate(0.5f, BOARD_TOP + 0.02f, 0.5f);
            poseStack.mulPose(Axis.YP.rotationDegrees(-boardRotation));
            poseStack.translate(SURFACE_OFFSET, 0f, 0f);
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

    // ---------- The knife during the close-up: it hovers over the board and comes down when you chop ----------
    public static final float KNIFE_SCALE = 0.34f;
    public static final float KNIFE_ROLL = 20f; // degrees rolled around its length so you see the flat of the blade
    public static final float KNIFE_HOVER = 0.18f;

    public static float knifeLift(ChoppingBoardBlockEntity board) {
        float t = (System.currentTimeMillis() - board.clientChopStart) / 180f; // 0..1 during a chop
        if (t >= 1f) {
            return KNIFE_HOVER;
        }
        return KNIFE_HOVER * Math.abs(1f - 2f * t); // down, then back up
    }

    // Held straight: blade level with the board, edge down, handle towards you (right hand)
    private void renderKnife(ChoppingBoardBlockEntity board, PoseStack poseStack, MultiBufferSource buffers, int light) {
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockPos pos = board.getBlockPos();
        double hx = cam.x - (pos.getX() + 0.5);
        double hz = cam.z - (pos.getZ() + 0.5);
        poseStack.pushPose();
        poseStack.translate(0.5 + board.clientKnifeX, BOARD_TOP + 0.04 + knifeLift(board), 0.5 + board.clientKnifeZ);
        // Pointing straight away from you: handle towards you, tip away from you
        poseStack.mulPose(Axis.YP.rotation((float) Math.atan2(hz, -hx)));   // icon +X (the tip) -> away from you
        // Turned over around its length so the sharp edge faces the board, rolled a little so the flat shows
        poseStack.mulPose(Axis.XP.rotationDegrees(180f + KNIFE_ROLL));
        poseStack.scale(KNIFE_SCALE, KNIFE_SCALE, KNIFE_SCALE);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-135f));                // icon diagonal -> level (handle at -X, tip at +X)
        Minecraft.getInstance().getItemRenderer().renderStatic(board.clientKnife, ItemDisplayContext.FIXED, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffers, board.getLevel(), 97);
        poseStack.popPose();
    }
}
