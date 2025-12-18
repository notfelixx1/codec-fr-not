package nott.notarmy.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class ArmorStatus {

    public static boolean isEnabled = true;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!isEnabled) return;
        // Рисуем только в слое HOTBAR (чтобы не перекрывать меню и не рисоваться дважды)
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode.hasInfiniteItems()) return; // В креативе не нужно

        GuiGraphics context = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        // Позиция: Справа снизу, над едой
        int x = width / 2 + 10; // Смещение вправо от центра
        int y = height - 55;    // Высота

        Player player = mc.player;

        // Рисуем броню (Шлем, Нагрудник, Штаны, Ботинки)
        // В инвентаре броня идет в обратном порядке (3, 2, 1, 0)
        int itemOffset = 0;
        for (ItemStack item : player.getInventory().armor) {
            if (item.isEmpty()) continue;

            // Рисуем предмет
            context.renderItem(item, x + (itemOffset * 20), y);
            // Рисуем прочность (полоску и цифры)
            context.renderItemDecorations(mc.font, item, x + (itemOffset * 20), y);

            itemOffset++;
        }

        // Рисуем предметы в руках (Главная и Вторая)
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (!mainHand.isEmpty()) {
            context.renderItem(mainHand, x + (itemOffset * 20), y);
            context.renderItemDecorations(mc.font, mainHand, x + (itemOffset * 20), y);
            itemOffset++;
        }

        if (!offHand.isEmpty()) {
            context.renderItem(offHand, x + (itemOffset * 20), y);
            context.renderItemDecorations(mc.font, offHand, x + (itemOffset * 20), y);
        }
    }
}