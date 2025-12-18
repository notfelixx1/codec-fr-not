package nott.notarmy.Combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class MultiClicker {

    // ВМЕСТО clickMode используем стандартный isEnabled
    public static boolean isEnabled = false;

    public static int minCps = 8;
    public static int maxCps = 12;

    private static long lastClickTime = 0;
    private static long nextDelay = 0;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Проверка: Если выключено - выходим
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return;

        if (GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastClickTime >= nextDelay) {
                mc.player.swing(InteractionHand.MAIN_HAND);

                if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
                    mc.gameMode.attack(mc.player, ((net.minecraft.world.phys.EntityHitResult)mc.hitResult).getEntity());
                } else {
                    mc.player.resetAttackStrengthTicker();
                }

                lastClickTime = currentTime;
                updateDelay();
            }
        } else {
            lastClickTime = 0;
        }
    }

    private static void updateDelay() {
        int min = Math.min(minCps, maxCps);
        int max = Math.max(minCps, maxCps);
        if (min < 1) min = 1;
        int cps = random.nextInt(max - min + 1) + min;
        nextDelay = 1000L / cps;
    }
}