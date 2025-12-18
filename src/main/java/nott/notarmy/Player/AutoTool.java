package nott.notarmy.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class AutoTool {

    public static boolean isEnabled = false;

    private static int originalSlot = -1; // Запоминаем старый слот
    private static boolean wasMining = false; // Флаг, что мы копали

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Проверяем, зажата ли ЛКМ
        boolean isClicking = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;

        // Проверяем, смотрим ли мы на блок
        boolean lookingAtBlock = mc.hitResult != null && mc.hitResult.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK;

        if (isClicking && lookingAtBlock) {
            // Начинаем или продолжаем копать
            if (!wasMining) {
                originalSlot = mc.player.getInventory().selected; // Запоминаем, что было в руках (например, меч)
                wasMining = true;
            }

            // Логика выбора инструмента
            // (Используем координаты блока из хит-результата)
            net.minecraft.core.BlockPos pos = ((net.minecraft.world.phys.BlockHitResult)mc.hitResult).getBlockPos();
            BlockState state = mc.level.getBlockState(pos);

            if (state.getDestroySpeed(mc.level, pos) > 0) {
                int bestSlot = findBestTool(mc, state);
                if (bestSlot != -1 && bestSlot != mc.player.getInventory().selected) {
                    mc.player.getInventory().selected = bestSlot;
                }
            }

        } else {
            // Если мы перестали копать (отпустили кнопку или отвели взгляд)
            if (wasMining) {
                // Если у нас сохранен слот и он корректен
                if (originalSlot != -1 && originalSlot < 9) {
                    mc.player.getInventory().selected = originalSlot; // ВОЗВРАЩАЕМ ПРЕДМЕТ
                }
                wasMining = false;
                originalSlot = -1;
            }
        }
    }

    private static int findBestTool(Minecraft mc, BlockState state) {
        int bestSlot = -1;
        float bestSpeed = 1.0f;

        // Если текущий инструмент уже хорош, берем его скорость за базу
        ItemStack current = mc.player.getMainHandItem();
        bestSpeed = current.getDestroySpeed(state);
        bestSlot = mc.player.getInventory().selected;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }
}