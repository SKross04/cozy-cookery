package net.skross.cozycookery.client;

import net.minecraft.client.CameraType;
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
    private static Vec3 eye = Vec3.ZERO;
    private static Vec3 target = Vec3.ZERO;
    // the view (first person / F5 views) from before the close-up, restored afterwards
    @Nullable
    private static CameraType previousView = null;

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
    public static void start(Vec3 lookAt, double distance, double height) {
        Vec3 target = lookAt;
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

        CloseUpCamera.eye = eye;
        CloseUpCamera.target = target;
        Marker marker = new Marker(EntityType.MARKER, mc.level);
        marker.moveTo(eye.x, eye.y, eye.z, yaw, pitch);
        marker.xo = marker.xOld = eye.x;
        marker.yo = marker.yOld = eye.y;
        marker.zo = marker.zOld = eye.z;
        marker.yRotO = yaw;
        marker.xRotO = pitch;
        camera = marker;
        // F5 views would put the camera behind/in front of the marker: always look through it in first person
        if (previousView == null) {
            previousView = mc.options.getCameraType();
        }
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.setCameraEntity(marker);
    }

    public static void start(BlockPos pos, double surfaceHeight) {
        start(new Vec3(pos.getX() + 0.5, pos.getY() + surfaceHeight, pos.getZ() + 0.5), 0.55, 0.95);
    }

    // ---------- Screen <-> world (simple pinhole camera, matches the game view closely enough) ----------
    private static Vec3 forward() {
        return target.subtract(eye).normalize();
    }

    private static Vec3 right() {
        return forward().cross(new Vec3(0, 1, 0)).normalize();
    }

    private static Vec3 up() {
        return right().cross(forward());
    }

    // pixels (GUI scale) per unit at distance 1
    private static double focal(int screenHeight) {
        double fov = Minecraft.getInstance().options.fov().get();
        return (screenHeight / 2.0) / Math.tan(Math.toRadians(fov) / 2.0);
    }

    /** Where the mouse points on the horizontal plane through the target (e.g. the soup surface). */
    public static Vec3 mouseOnSurface(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        double f = focal(screenHeight);
        double sx = (mouseX - screenWidth / 2.0) / f;
        double sy = (mouseY - screenHeight / 2.0) / f;
        Vec3 dir = forward().add(right().scale(sx)).subtract(up().scale(sy));
        if (Math.abs(dir.y) < 1.0E-4) {
            return target;
        }
        double t = (target.y - eye.y) / dir.y;
        return eye.add(dir.scale(t));
    }

    /** Screen position (GUI scale) of a point in the world. */
    public static double[] toScreen(Vec3 point, int screenWidth, int screenHeight) {
        Vec3 v = point.subtract(eye);
        double z = v.dot(forward());
        double f = focal(screenHeight);
        return new double[]{screenWidth / 2.0 + v.dot(right()) / z * f, screenHeight / 2.0 - v.dot(up()) / z * f};
    }

    public static Vec3 target() {
        return target;
    }

    public static void stop() {
        Minecraft mc = Minecraft.getInstance();
        if (camera != null && mc.player != null) {
            mc.setCameraEntity(mc.player);
        }
        if (previousView != null) {
            mc.options.setCameraType(previousView);
        }
        camera = null;
        previousView = null;
    }
}
