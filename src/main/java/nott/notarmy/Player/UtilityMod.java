package nott.notarmy.Player;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class UtilityMod {

    public static boolean isSprintEnabled = false;
    public static boolean isFullBrightEnabled = false;
    public static boolean isFastPlaceEnabled = false;

    // Поле для рефлексии (кэшируем, чтобы не лагало)
    private static Field delayField = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // --- FastPlace (Через рефлексию) ---
        if (isFastPlaceEnabled) {
            try {
                if (delayField == null) {
                    // Пытаемся найти поле rightClickDelay (Mojang mapping)
                    try {
                        delayField = Minecraft.class.getDeclaredField("rightClickDelay");
                    } catch (NoSuchFieldException e) {
                        // Если не нашли, пробуем SRG имя (f_91055_)
                        delayField = Minecraft.class.getDeclaredField("f_91055_");
                    }
                    delayField.setAccessible(true); // Взламываем доступ
                }
                // Устанавливаем задержку в 0
                delayField.setInt(mc, 0);
            } catch (Exception e) {
                // Если не получилось - ничего не делаем, чтобы не крашить игру
                e.printStackTrace();
            }
        }
        if (isSprintEnabled) {
            if (mc.options.keyUp.isDown() && !mc.player.isShiftKeyDown() && !mc.player.horizontalCollision) {
                KeyMapping.set(mc.options.keySprint.getKey(), true);
            }
        }
        if (isFullBrightEnabled && mc.player.tickCount % 10 == 0) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 0, false, false));
        }
    }
}