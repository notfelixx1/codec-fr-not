package nott.notarmy.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends Screen {

    private final List<Panel> panels = new ArrayList<>();
    private final ParticleEngine particles = new ParticleEngine(60);
    private boolean initialized = false;

    public ClickGuiScreen() {
        super(Component.literal("NotArmy GUI"));
        ModuleManager.init();
    }

    @Override
    protected void init() {
        if (!initialized) {
            int xOffset = 20;
            for (ModuleManager.Category category : ModuleManager.Category.values()) {
                Panel panel = new Panel(category.name(), xOffset, 20, 110, 18, category);

                for (ModuleManager.Module m : ModuleManager.modules) {
                    if (m.category == category) {
                        panel.addButton(new ModuleButton(m));
                    }
                }

                panels.add(panel);
                xOffset += 125;
            }
            initialized = true;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(context, mouseX, mouseY, partialTick);
        particles.render(context, this.width, this.height, mouseX, mouseY);

        for (Panel panel : panels) {
            panel.render(context, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Клик по панелям (сверху вниз)
        for (int i = panels.size() - 1; i >= 0; i--) {
            Panel panel = panels.get(i);
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                // Поднимаем панель наверх, если кликнули по шапке
                if (button == 0 && panel.isHoveredHeader(mouseX, mouseY)) {
                    panels.remove(i);
                    panels.add(panel);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Panel panel : panels) panel.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (Panel panel : panels) panel.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}