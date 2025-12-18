package nott.notarmy.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class NoParticles {

    public static boolean isEnabled = false;

    // Переменная для хранения "взломанного" метода, чтобы не искать его каждый тик
    private static Method clearMethod = null;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        try {
            // Если метод еще не найден, ищем его
            if (clearMethod == null) {
                try {
                    // Пытаемся найти метод по нормальному имени (Mojang)
                    clearMethod = ParticleEngine.class.getDeclaredMethod("clearParticles");
                } catch (NoSuchMethodException e) {
                    // Если не нашли, пробуем зашифрованное имя (SRG/Obfuscated) для 1.20.4
                    clearMethod = ParticleEngine.class.getDeclaredMethod("m_107338_");
                }
                // Разрешаем доступ (взламываем private)
                clearMethod.setAccessible(true);
            }

            // Вызываем метод
            clearMethod.invoke(mc.particleEngine);

        } catch (Exception e) {
            // Если произошла ошибка, выключаем модуль, чтобы не спамить в консоль
            e.printStackTrace();
            isEnabled = false;
        }
    }
}