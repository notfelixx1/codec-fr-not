package nott.notarmy.Render;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class NoWeather {

    public static boolean isEnabled = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // Если идет дождь (уровень > 0), выключаем его
        if (mc.level.getRainLevel(1.0f) > 0.0f) {
            mc.level.setRainLevel(0.0f);
        }

        // Если гроза, выключаем её
        if (mc.level.getThunderLevel(1.0f) > 0.0f) {
            mc.level.setThunderLevel(0.0f);
        }
    }
}