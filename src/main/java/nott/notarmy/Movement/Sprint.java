package nott.notarmy.movement;

import nott.notarmy.Notarmy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Notarmy.MOD_ID)
public class Sprint {

    private static boolean enabled = true; // можно будет переключать через gui/кнопку

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!enabled) return;
        if (event.player == null) return;
        if (event.phase != TickEvent.Phase.START) return;

        // Включаем спринт, если игрок двигается вперёд и не крадётся/летит
        if (event.player.moveForward > 0 && !event.player.isSneaking() && !event.player.isCrouching() && !event.player.isUsingItem()) {
            event.player.setSprinting(true);
        }
    }

    // Метод для тоггла из ClickGUI (по желанию)
    public static void toggle() {
        enabled = !enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
