package nott.notarmy.Movement;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AutoJump {

    public static boolean isEnabled = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Если мы на земле и не в воде/лаве (чтобы не прыгать на поверхности воды как дурак)
        if (mc.player.onGround() && !mc.player.isInWater() && !mc.player.isInLava()) {
            mc.player.jumpFromGround();
        }
    }
}