package nott.notarmy.Movement;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AirJump {

    public static boolean isEnabled = false;

    // Срабатывает при нажатии клавиши
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (!isEnabled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Если нажали Пробел (Space) и мы в игре (не в чате)
        if (event.getKey() == GLFW.GLFW_KEY_SPACE && event.getAction() == GLFW.GLFW_PRESS && mc.screen == null) {

            // Если мы НЕ на земле и не в воде (в воде обычное плавание)
            if (!mc.player.onGround() && !mc.player.isInWater()) {
                // Принудительно прыгаем
                mc.player.jumpFromGround();
            }
        }
    }
}