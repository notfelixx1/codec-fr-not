package nott.notarmy.Movement;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AutoWalk {

    public static boolean isEnabled = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (isEnabled) {
            // Зажимаем кнопку "Вперед"
            KeyMapping.set(mc.options.keyUp.getKey(), true);
        } else {
            // Если мод выключен, мы не должны мешать игроку.
            // Но и "отжимать" кнопку насильно нельзя, вдруг игрок сам её держит.
            // Поэтому тут просто ничего не делаем, KeyMapping сбросится сам, когда игрок отпустит клавишу физически.
        }
    }

    // Метод для корректного выключения через GUI (чтобы перс остановился)
    public static void toggle() {
        isEnabled = !isEnabled;
        if (!isEnabled) {
            Minecraft mc = Minecraft.getInstance();
            // Принудительно отжимаем кнопку при выключении
            KeyMapping.set(mc.options.keyUp.getKey(), false);
        }
    }
}