package nott.notarmy.Render;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class ZoomMod {

    // Создаем кнопку C (без регистрации в KeyHandler, сделаем "на лету" для простоты,
    // но лучше добавить в KeyHandler как остальные)
    public static final KeyMapping ZOOM_KEY = new KeyMapping("key.notarmy.zoom", GLFW.GLFW_KEY_C, "category.notarmy.general");

    static {
        // Регистрируем кнопку (костыль для быстрого добавления, по-хорошему надо через событие RegisterKeyMappings)
        net.minecraft.client.Minecraft.getInstance().options.keyMappings =
                org.apache.commons.lang3.ArrayUtils.add(net.minecraft.client.Minecraft.getInstance().options.keyMappings, ZOOM_KEY);
    }

    @SubscribeEvent
    public static void onComputeFov(ComputeFovModifierEvent event) {
        // Если кнопка нажата
        if (ZOOM_KEY.isDown()) {
            // Уменьшаем FOV в 4 раза (Zoom x4)
            event.setNewFovModifier(event.getNewFovModifier() / 4.0f);
        }
    }
}