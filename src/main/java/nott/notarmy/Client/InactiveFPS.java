package nott.notarmy.Client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class InactiveFPS {

    public static boolean isEnabled = true;

    // Храним оригинальный FPS. -1 означает, что мы сейчас "в активном режиме"
    private static int originalLimit = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options == null) return;

        // Если окно игры НЕ активно (свернуто или мышка в другом окне)
        if (!mc.isWindowActive()) {
            // Если мы еще не сохранили оригинальный FPS (значит, только что свернули)
            if (originalLimit == -1) {
                // 1. Запоминаем, сколько было (например, 120 или 260)
                originalLimit = mc.options.framerateLimit().get();

                // 2. Ставим 10 FPS (Это минимум, который разрешает игра без ошибок)
                // Важно делать это только ОДИН раз при сворачивании, а не каждый тик
                mc.options.framerateLimit().set(10);
            }
        }
        // Если окно СНОВА активно (вернулись в игру)
        else {
            // Если у нас сохранен старый лимит (значит, мы были свернуты)
            if (originalLimit != -1) {
                // Восстанавливаем как было
                mc.options.framerateLimit().set(originalLimit);
                // Сбрасываем флаг
                originalLimit = -1;
            }
        }
    }
}