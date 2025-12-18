package nott.notarmy.Movement;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class Spider {

    public static boolean isEnabled = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Если уперлись в стену горизонтально (horizontalCollision)
        if (mc.player.horizontalCollision) {
            Vec3 motion = mc.player.getDeltaMovement();

            // Если мы не падаем слишком быстро (чтобы не застрять)
            if (motion.y < 0.2) {
                // Подталкиваем вверх (скорость как по лестнице)
                mc.player.setDeltaMovement(motion.x, 0.2, motion.z);
            }
        }
    }
}