package net.skross.cozycookery.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Close-up camera for the cooking stations (client only).
 * While a close-up is open, the game looks through an invisible marker entity that hangs above the station
 * and looks down into it. Your character stays where it is. Closing the screen puts the camera back.
 */
public final class CloseUpCamera {
    @Nullable
    private static Marker camera = null;

    private CloseUpCamera() {
    }

    public static boolean isActive() {
        return camera != null;
    }

    /**
     * @param target the point to look at (e.g. the middle of the soup)
     * @param distance how far back from the target the camera sits (towards the player)
     * @param height how far above the target the camera sits
     */
    public static void start(Vec3 target, double distance, double height) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        // Come from the side the player is standing on, so the view feels natural
        Vec3 toPlayer = mc.player.position().subtract(target).multiply(1, 0, 1);
        if (toPlayer.lengthSqr() < 1.0E-4) {
            toPlayer = new Vec3(0, 0, 1);
        }
        toPlayer = toPlayer.normalize();
        Vec3 eye = target.add(toPlayer.scale(distance)).add(0, height, 0);

        Vec3 look = target.subtract(eye);
        float yaw = (float) (Mth.atan2(look.z, look.x) * Mth.RAD_TO_DEG) - 90.0f;
        float pitch = (float) -(Mth.atan2(look.y, look.horizontalDistance()) * Mth.RAD_TO_DEG);

        Marker marker = new Marker(EntityType.MARKER, mc.level);
        marker.moveTo(eye.x, eye.y, eye.z, yaw, pitch);
        marker.xo = marker.xOld = eye.x;
        marker.yo = marker.yOld = eye.y;
        marker.zo = marker.zOld = eye.z;
        marker.yRotO = yaw;
        marker.xRotO = pitch;
        camera = marker;
        mc.setCameraEntity(marker);
    }

    public static void start(BlockPos pos, double surfaceHeight) {
        start(new Vec3(pos.getX() + 0.5, pos.getY() + surfaceHeight, pos.getZ() + 0.5), 0.55, 0.95);
    }

    public static void stop() {
        Minecraft mc = Minecraft.getInstance();
        if (camera != null && mc.player != null) {
            mc.setCameraEntity(mc.player);
        }
        camera = null;
    }
}
