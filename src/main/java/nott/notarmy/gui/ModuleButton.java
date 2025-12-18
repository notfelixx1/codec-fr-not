package nott.notarmy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;

public class ModuleButton {

    public final ModuleManager.Module module;
    public boolean settingsOpen = false;
    private final List<ComponentBase> settings = new ArrayList<>();

    public ModuleButton(ModuleManager.Module module) {
        this.module = module;
        // Заполняем настройки
        for (ModuleManager.NumberSetting num : module.numSettings) {
            settings.add(new SliderComponent(num));
        }
        for (ModuleManager.BooleanSetting bool : module.boolSettings) {
            settings.add(new CheckboxComponent(bool));
        }
    }

    public void render(GuiGraphics context, double x, double y, double w, int mx, int my) {
        boolean enabled = module.getter.get();
        boolean hovered = mx >= x && mx <= x + w && my >= y && my <= y + 16;

        int color = enabled ? 0xFF0090FF : (hovered ? 0xFF303030 : 0xFF202020);

        RenderUtils.drawRect(context, x, y, w, 16, color);
        context.drawString(Minecraft.getInstance().font, module.name, (int)(x + 4), (int)(y + 4), enabled ? 0xFFFFFFFF : 0xFFAAAAAA, true);

        if (!settings.isEmpty()) {
            String arrow = settingsOpen ? "-" : "+";
            context.drawString(Minecraft.getInstance().font, arrow, (int)(x + w - 10), (int)(y + 4), 0xFF808080, false);
        }

        if (settingsOpen) {
            double setY = y + 16;
            for (ComponentBase comp : settings) {
                comp.render(context, x, setY, w, mx, my);
                setY += comp.getHeight();
            }
        }
    }

    public boolean mouseClicked(double x, double y, double w, double mx, double my, int btn) {
        // Клик по кнопке модуля
        if (mx >= x && mx <= x + w && my >= y && my <= y + 16) {
            if (btn == 0) { // ЛКМ - Тогл
                module.toggle();
                Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            } else if (btn == 1) { // ПКМ - Настройки
                settingsOpen = !settingsOpen;
                Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }

        // Клик по настройкам
        if (settingsOpen) {
            double setY = y + 16;
            for (ComponentBase comp : settings) {
                if (comp.mouseClicked(x, setY, w, mx, my, btn)) return true;
                setY += comp.getHeight();
            }
        }
        return false;
    }

    public void mouseReleased(double mx, double my, int btn) {
        if (settingsOpen) {
            for (ComponentBase c : settings) c.mouseReleased(mx, my, btn);
        }
    }

    public void mouseDragged(double mx, double my, double dx, double dy) {
        if (settingsOpen) {
            for (ComponentBase c : settings) c.mouseDragged(mx, my, dx, dy);
        }
    }

    public double getHeight() {
        double h = 16;
        if (settingsOpen) {
            for (ComponentBase c : settings) h += c.getHeight();
        }
        return h;
    }
}