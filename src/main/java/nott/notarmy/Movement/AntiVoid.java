package nott.notarmy.Movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AntiVoid {

    public static boolean isEnabled = false;

    // Настройка: если Y меньше этого числа, спасаем
    // -64 это дно мира в 1.18+, но для безопасности ставим чуть выше пустоты, например -70 или 0 в Энде
    public static double triggerY = -70.0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Если мы падаем (motionY < 0) и высота ниже критической
        if (mc.player.getY() < triggerY && mc.player.getDeltaMovement().y < 0) {
            Vec3 motion = mc.player.getDeltaMovement();

            // Подбрасываем вверх и обнуляем падение
            // Y = 1.0 (сильный прыжок)
            mc.player.setDeltaMovement(motion.x, 1.5, motion.z);
            mc.player.fallDistance = 0; // Сбрасываем урон от падения
        }
    }
}