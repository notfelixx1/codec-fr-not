package nott.notarmy.Combat;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class Velocity {
    public static boolean isEnabled = false;

    // Множители (0.0 - 1.0)
    public static double horizontal = 0.0;
    public static double vertical = 0.0;

    // Шанс срабатывания (100% = всегда работает, 90% = иногда пропускает удар)
    // Это "God Mode" для обхода античитов. Если поставить 85-90%, античит никогда не забанит.
    public static double chance = 100.0;
}