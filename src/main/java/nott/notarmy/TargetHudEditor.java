package nott.notarmy;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nott.notarmy.Render.TargetHud;

public class TargetHudEditor extends Screen {

    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public TargetHudEditor() {
        super(Component.literal("TargetHUD Editor"));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(context, mouseX, mouseY, partialTick);

        // Рисуем инструкцию по центру
        context.drawCenteredString(this.font, "Перетащи TargetHUD мышкой", this.width / 2, 20, 0xFFFFFF);
        context.drawCenteredString(this.font, "Нажми ESC для сохранения", this.width / 2, 35, 0xAAAAAA);

        // TargetHud сам отрисует себя, потому что в TargetHud.java мы добавили проверку:
        // else if (mc.screen instanceof TargetHudEditor)

        // Но нам нужно нарисовать рамку вокруг него, чтобы было видно границы
        int x = TargetHud.x;
        int y = TargetHud.y;
        int w = TargetHud.width;
        int h = TargetHud.height;

        // Зеленая рамка редактирования
        context.renderOutline(x - 1, y - 1, w + 2, h + 2, 0xFF00FF00);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // ЛКМ
            int x = TargetHud.x;
            int y = TargetHud.y;
            int w = TargetHud.width;
            int h = TargetHud.height;

            // Если кликнули по панели
            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                isDragging = true;
                dragOffsetX = (int) (mouseX - x);
                dragOffsetY = (int) (mouseY - y);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            // Обновляем координаты в самом классе TargetHud
            TargetHud.x = (int) (mouseX - dragOffsetX);
            TargetHud.y = (int) (mouseY - dragOffsetY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean isPauseScreen() {
        return true; // Ставим паузу, чтобы удобно настраивать
    }
}