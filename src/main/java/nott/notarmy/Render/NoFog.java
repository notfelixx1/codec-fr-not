package nott.notarmy.Render;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class NoFog {

    public static boolean isEnabled = false;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (!isEnabled) return;

        // Отменяем рендер тумана
        event.setCanceled(true);

        // Дополнительно отодвигаем туман в бесконечность, чтобы наверняка
        event.setNearPlaneDistance(Float.MAX_VALUE);
        event.setFarPlaneDistance(Float.MAX_VALUE);
    }
}