package nott.notarmy.Client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class MemoryFix {

    public static boolean isEnabled = true;
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        tickCounter++;

        // Чистим память раз в 60 секунд (1200 тиков)
        if (tickCounter >= 1200) {
            tickCounter = 0;
            System.gc(); // Вызов сборщика мусора Java
        }
    }
}