package nott.notarmy.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

import java.awt.Color;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class InfoHud {

    public static boolean isEnabled = true;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!isEnabled) return;
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        GuiGraphics context = event.getGuiGraphics();
        int height = mc.getWindow().getGuiScaledHeight();

        // Координаты
        int x = (int) mc.player.getX();
        int y = (int) mc.player.getY();
        int z = (int) mc.player.getZ();

        // FPS
        String fps = mc.fpsString.split(" ")[0]; // Берем только цифру

        // Формируем текст
        String coords = String.format("XYZ: %d %d %d", x, y, z);
        String fpsText = "FPS: " + fps;

        // Рисуем слева снизу
        int textY = height - 25; // чуть выше чата

        // Рисуем FPS
        context.drawString(mc.font, fpsText, 2, textY, new Color(200, 200, 200).getRGB(), true);
        // Рисуем Координаты чуть ниже
        context.drawString(mc.font, coords, 2, textY + 10, new Color(200, 200, 200).getRGB(), true);
    }
}