package nott.notarmy.Render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.KeyHandler;
import nott.notarmy.Notarmy;
import nott.notarmy.gui.ClickGuiScreen;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class ClientEvents {

    public static boolean isEspEnabled = false;

    // Метод для включения свечения (Рефлексия)
    private static void setGlowingForce(Entity entity, boolean value) {
        try {
            Method method;
            try {
                method = Entity.class.getDeclaredMethod("setSharedFlag", int.class, boolean.class);
            } catch (NoSuchMethodException e) {
                // m_20115_ - это setSharedFlag в 1.20.4
                method = Entity.class.getDeclaredMethod("m_20115_", int.class, boolean.class);
            }
            method.setAccessible(true);
            method.invoke(entity, 6, value);
        } catch (Exception e) {}
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // --- ОТКРЫТИЕ МЕНЮ ---
        if (KeyHandler.MENU_KEY.consumeClick()) {
            mc.setScreen(new ClickGuiScreen());
        }

        if (isEspEnabled) {
            for (Player player : mc.level.players()) {
                if (player == mc.player) continue;
                setGlowingForce(player, player.distanceTo(mc.player) <= 75);
            }
        }
    }
}