package nott.notarmy.Player;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AutoMineEvent {

    public static boolean isMining = false;

    // НОВОЕ: Настройки времени
    public static double maxDurationSeconds = 10.0; // По умолчанию 10 сек
    private static long startTime = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (isMining) {
            // Проверка таймера
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > (maxDurationSeconds * 1000)) {
                toggle(); // Выключаем, если время вышло
                mc.player.displayClientMessage(Component.literal("AutoMine: Таймер истек!"), true);
                return;
            }

            if (mc.screen == null) {
                KeyMapping.set(mc.options.keyAttack.getKey(), true);
            }
        }
    }

    // Вынесли переключение в отдельный метод
    public static void toggle() {
        isMining = !isMining;
        Minecraft mc = Minecraft.getInstance();
        if (isMining) {
            startTime = System.currentTimeMillis(); // Засекаем время
            if (mc.player != null)
                mc.player.displayClientMessage(Component.literal("AutoMine: ВКЛ (на " + (int)maxDurationSeconds + " сек)"), true);
        } else {
            if (mc.player != null)
                mc.player.displayClientMessage(Component.literal("AutoMine: ВЫКЛ"), true);
            KeyMapping.set(mc.options.keyAttack.getKey(), false);
        }
    }
}