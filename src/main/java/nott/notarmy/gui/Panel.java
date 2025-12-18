package nott.notarmy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Panel {
    public String title;
    public double x, y, w, h;
    public ModuleManager.Category category;

    public boolean extended = true;
    public boolean dragging = false;
    public double dragX, dragY;

    public final List<ModuleButton> buttons = new ArrayList<>();

    public Panel(String title, double x, double y, double w, double h, ModuleManager.Category category) {
        this.title = title;
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.category = category;
    }

    public void addButton(ModuleButton btn) {
        buttons.add(btn);
    }

    public void render(GuiGraphics context, int mx, int my, float partialTick) {
        if (dragging) {
            x = mx + dragX;
            y = my + dragY;
        }

        int headerColor = new Color(0, 150, 255).getRGB();

        // Шапка
        RenderUtils.drawRoundedRect(context, x, y, w, h, 4, 0xFF101010);
        RenderUtils.drawOutline(context, x, y, w, h, 4, headerColor);
        RenderUtils.drawCenteredString(context, Minecraft.getInstance().font, title, x + w / 2, y + 5, headerColor);

        if (extended) {
            double startY = y + h;
            // Для фона можно посчитать общую высоту
            // double totalHeight = 0;
            // for (ModuleButton b : buttons) totalHeight += b.getHeight();
            // RenderUtils.drawRect(context, x + 2, startY, w - 4, totalHeight + 2, 0xBB000000);

            for (ModuleButton moduleButton : buttons) {
                moduleButton.render(context, x + 2, startY, w - 4, mx, my);
                startY += moduleButton.getHeight();
            }

            // Линия внизу
            RenderUtils.drawRect(context, x, startY, w, 2, headerColor);
        }
    }

    public boolean mouseClicked(double mx, double my, int btn) {
        // Клик по шапке
        if (isHoveredHeader(mx, my)) {
            if (btn == 0) {
                dragging = true;
                dragX = x - mx;
                dragY = y - my;
                return true;
            } else if (btn == 1) {
                extended = !extended;
                return true;
            }
        }

        // Клик по модулям
        if (extended) {
            double startY = y + h;
            // ИСПРАВЛЕНИЕ: Переименовали переменную цикла в moduleButton, чтобы не путать с int btn
            for (ModuleButton moduleButton : buttons) {
                if (moduleButton.mouseClicked(x + 2, startY, w - 4, mx, my, btn)) return true;
                startY += moduleButton.getHeight();
            }
        }
        return false;
    }

    public void mouseReleased(double mx, double my, int btn) {
        dragging = false;
        if (extended) {
            for (ModuleButton b : buttons) b.mouseReleased(mx, my, btn);
        }
    }

    public void mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (extended) {
            for (ModuleButton b : buttons) b.mouseDragged(mx, my, dx, dy);
        }
    }

    public boolean isHoveredHeader(double mx, double my) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}