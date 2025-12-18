package nott.notarmy.Combat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

import java.util.Comparator;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AimAssist {

    public static boolean isEnabled = false;

    // Настройки
    public static double range = 4.5;   // Дистанция
    public static double speed = 0.2;   // Скорость наводки (0.1 - медленно, 1.0 - моментально)
    public static double fov = 60.0;    // Угол обзора (на 360 градусов не наводится)

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Работаем только если держим оружие (меч/топор)
        // if (!isHoldingWeapon(mc.player)) return; // Можно раскомментировать, если нужно

        // Ищем цель: Игрок, не мы, живой, в радиусе
        Player target = mc.level.players().stream()
                .filter(p -> p != mc.player)
                .filter(p -> !p.isSpectator())
                .filter(p -> p.distanceTo(mc.player) <= range)
                .min(Comparator.comparingDouble(p -> p.distanceTo(mc.player)))
                .orElse(null);

        if (target != null) {
            // Проверяем, в поле зрения ли он (чтобы не разворачиваться на 180 градусов спиной)
            float[] rotations = getRotations(target, mc);
            float yawDiff = Math.abs(Mth.wrapDegrees(rotations[0] - mc.player.getYRot()));

            if (yawDiff <= fov) {
                // Плавная доводка
                smoothRotate(rotations[0], rotations[1], mc);
            }
        }
    }

    private static void smoothRotate(float targetYaw, float targetPitch, Minecraft mc) {
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();

        // Интерполяция углов
        float yawDiff = Mth.wrapDegrees(targetYaw - yaw);
        float pitchDiff = Mth.wrapDegrees(targetPitch - pitch);

        // Применяем скорость (чем больше speed, тем резче)
        // Добавляем немного рандома, чтобы античит не спалил робота
        double randomSpeed = speed + (Math.random() * 0.1);

        // Если разница маленькая, доводим сразу, если большая - плавно
        if (Math.abs(yawDiff) > 1.0) {
            mc.player.setYRot((float) (yaw + yawDiff * randomSpeed));
        }
        if (Math.abs(pitchDiff) > 1.0) {
            mc.player.setXRot((float) (pitch + pitchDiff * randomSpeed));
        }
    }

    // Математика вычисления углов до цели
    private static float[] getRotations(Player target, Minecraft mc) {
        double x = target.getX() - mc.player.getX();
        double z = target.getZ() - mc.player.getZ();
        // Целимся в грудь/голову (EyeHeight - 0.2)
        double y = (target.getY() + target.getEyeHeight() - 0.2) - (mc.player.getY() + mc.player.getEyeHeight());

        double dist = Math.sqrt(x * x + z * z);

        float yaw = (float) (Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) -(Math.atan2(y, dist) * 180.0D / Math.PI);

        return new float[]{yaw, pitch};
    }
}