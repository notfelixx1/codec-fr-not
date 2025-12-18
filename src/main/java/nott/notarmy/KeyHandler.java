package nott.notarmy;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
    public static final KeyMapping MENU_KEY = new KeyMapping(
            "key.clickgui.open",
            GLFW.GLFW_KEY_RIGHT_SHIFT, // Правый Шифт
            "category.notarmy.general"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MENU_KEY);
    }
}