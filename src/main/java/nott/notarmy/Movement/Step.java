package nott.notarmy.Movement;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class Step {

    public static boolean isEnabled = false;
    public static float height = 1.0f; // Высота шага

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (isEnabled) {
            // Устанавливаем высоту
            mc.player.setMaxUpStep(height);
        } else {
            // ИСПРАВЛЕНИЕ: Используем maxUpStep() вместо getMaxUpStep()
            if (mc.player.maxUpStep() > 0.6f) {
                mc.player.setMaxUpStep(0.6f);
            }
        }
    }

    // Метод для корректного выключения через GUI
    public static void toggle() {
        isEnabled = !isEnabled;
        if (!isEnabled) {
            Minecraft mc = Minecraft.getInstance();
            // Возвращаем стандартную высоту при выключении
            if (mc.player != null) mc.player.setMaxUpStep(0.6f);
        }
    }
}