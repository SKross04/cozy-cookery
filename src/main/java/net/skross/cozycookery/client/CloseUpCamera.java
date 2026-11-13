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

    /** Look at a station from its front (e.g. into the oven), instead of from the player's side. */
    public static void startFront(BlockPos pos, net.minecraft.core.Direction front, double targetHeight,
                                  double distance, double height) {
        Vec3 target = new Vec3(pos.getX() + 0.5, pos.getY() + targetHeight, pos.getZ() + 0.5);
        Vec3 out = new Vec3(front.getStepX(), 0, front.getStepZ());
        place(target, target.add(out.scale(distance)).add(0, height, 0));
    }

    /**
     * @param lookAt the point to look at (e.g. the middle of the soup)
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

        place(target, eye);
    }

    private static void place(Vec3 target, Vec3 eye) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
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

    // ---------- Screen <-> world, using the game's real camera ----------
    private static Vec3 camPos() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    }

    private static Vec3 forward() {
        return new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getLookVector());
    }

    private static Vec3 up() {
        return new Vec3(Minecraft.getInstance().gameRenderer.getMainCamera().getUpVector());
    }

    private static Vec3 right() {
        return forward().cross(up()).normalize();
    }

    // pixels (GUI scale) per unit at distance 1
    private static double focal(int screenHeight) {
        double fov = Minecraft.getInstance().options.fov().get();
        return (screenHeight / 2.0) / Math.tan(Math.toRadians(fov) / 2.0);
    }

    /** Where the mouse points on the horizontal plane at the given height (e.g. the soup surface). */
    public static Vec3 mouseOnPlane(double mouseX, double mouseY, int screenWidth, int screenHeight, double planeY) {
        double f = focal(screenHeight);
        double sx = (mouseX - screenWidth / 2.0) / f;
        double sy = (mouseY - screenHeight / 2.0) / f;
        Vec3 dir = forward().add(right().scale(sx)).subtract(up().scale(sy));
        Vec3 eye = camPos();
        if (Math.abs(dir.y) < 1.0E-4) {
            return new Vec3(eye.x, planeY, eye.z);
        }
        double t = (planeY - eye.y) / dir.y;
        return eye.add(dir.scale(t));
    }

    /** Screen position (GUI scale) of a point in the world. */
    public static double[] toScreen(Vec3 point, int screenWidth, int screenHeight) {
        Vec3 v = point.subtract(camPos());
        double z = Math.max(0.01, v.dot(forward()));
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
