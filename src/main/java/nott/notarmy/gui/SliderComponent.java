package nott.notarmy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SliderComponent extends ComponentBase {

    private final ModuleManager.NumberSetting setting;
    private boolean dragging = false;

    public SliderComponent(ModuleManager.NumberSetting s) {
        this.setting = s;
    }

    @Override
    public void render(GuiGraphics context, double x, double y, double w, int mx, int my) {
        RenderUtils.drawRect(context, x, y, w, 16, 0xFF151515); // Фон

        if (dragging) {
            double diff = Math.min(w, Math.max(0, mx - x));
            double val = setting.min + (diff / w) * (setting.max - setting.min);
            if (setting.max > 20) val = Math.round(val);
            else val = Math.round(val * 100.0) / 100.0;
            setting.current = val;
            setting.onChange.accept(val);
        }

        double pct = (setting.current - setting.min) / (setting.max - setting.min);
        RenderUtils.drawRect(context, x, y, w * pct, 16, 0xFF0090FF); // Заполнение (Синий)

        String display = setting.name + ": " + setting.current;
        context.drawString(Minecraft.getInstance().font, display, (int)x + 4, (int)y + 4, 0xFFFFFFFF, true);
    }

    @Override
    public boolean mouseClicked(double x, double y, double w, double mx, double my, int btn) {
        if (mx >= x && mx <= x + w && my >= y && my <= y + 16 && btn == 0) {
            dragging = true;
            return true;
        }
        return false;
    }

    @Override
    public void mouseReleased(double mx, double my, int btn) {
        dragging = false;
    }

    @Override
    public double getHeight() {
        return 16;
    }
}