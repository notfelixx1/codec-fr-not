package nott.notarmy.Movement;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class NoSlow {

    public static boolean isEnabled = false;

    // Настройки
    public static boolean items = true;
    public static boolean webs = true;

    // 1. Анти-Замедление от ПРЕДМЕТОВ
    @SubscribeEvent
    public static void onInput(MovementInputUpdateEvent event) {
        if (!isEnabled || !items) return;

        if (event.getEntity().isUsingItem() && !event.getEntity().isPassenger()) {
            event.getInput().leftImpulse *= 5.0F;
            event.getInput().forwardImpulse *= 5.0F;
        }
    }

    // 2. Анти-Замедление от ПАУТИНЫ (Улучшенное)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled || !webs) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Если мы реально застряли (касаемся паутины)
        if (isCollidingWithWeb(mc)) {
            // Майнкрафт умножает скорость на 0.25 (X/Z) и 0.05 (Y)
            // Мы должны компенсировать это.

            Vec3 motion = mc.player.getDeltaMovement();

            // X и Z: Умножаем на 4.0, чтобы вернуть 100% скорости.
            // Ставим чуть больше (5.0), чтобы бежать даже быстрее, чем обычно.
            double x = motion.x * 5.0;
            double z = motion.z * 5.0;

            double y = motion.y;

            // Y (Вертикаль):
            // В паутине ты падаешь ооочень медленно.
            // Если мы падаем (y < 0), умножаем сильно (на 15), чтобы падать как обычно.
            if (y < 0) {
                y *= 15.0;
            }

            // Защита от "полетов" (чтобы античит не кикнул за превышение скорости)
            if (Math.abs(x) > 1.0) x /= 2.0;
            if (Math.abs(z) > 1.0) z /= 2.0;

            // Применяем новую скорость
            mc.player.setDeltaMovement(x, y, z);
        }
    }

    // Надежная проверка: Ищем паутину во всем хитбоксе игрока
    private static boolean isCollidingWithWeb(Minecraft mc) {
        if (mc.player == null) return false;

        // Получаем коробку игрока
        AABB box = mc.player.getBoundingBox();

        // Проходимся по всем блокам, которые задевает эта коробка
        // Math.floor округляет координаты до целого блока
        int minX = (int) Math.floor(box.minX);
        int minY = (int) Math.floor(box.minY);
        int minZ = (int) Math.floor(box.minZ);
        int maxX = (int) Math.floor(box.maxX);
        int maxY = (int) Math.floor(box.maxY);
        int maxZ = (int) Math.floor(box.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    // Если нашли хоть одну паутину - возвращаем true
                    if (mc.level.getBlockState(pos).is(Blocks.COBWEB)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}