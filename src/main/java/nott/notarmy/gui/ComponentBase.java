package nott.notarmy.gui;

import net.minecraft.client.gui.GuiGraphics;

public abstract class ComponentBase {
    public abstract void render(GuiGraphics context, double x, double y, double w, int mx, int my);
    public abstract boolean mouseClicked(double x, double y, double w, double mx, double my, int btn);
    public abstract double getHeight();
    public void mouseReleased(double mx, double my, int btn) {}
    public void mouseDragged(double mx, double my, double dx, double dy) {}
}
