package nott.notarmy.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Professional Config Manager UI
 * Style: Dark / Neon / Minimalistic
 * Features: Scissor, Smooth Scroll, Particles, Custom Input, Notifications
 */
public class ConfigScreen extends Screen {

    private final Screen parent;

    // --- LAYOUT ---
    private int windowX, windowY;
    private int windowWidth = 450;
    private int windowHeight = 300;

    // --- STATE ---
    private List<String> configs = new ArrayList<>();
    private String selectedConfig = null;
    private boolean initialized = false;

    // --- SCROLLING ---
    private float scrollOffset = 0;
    private float targetScroll = 0;
    private float maxScroll = 0;

    // --- COMPONENTS ---
    private ModernTextBox nameInput;
    private List<ModernButton> actionButtons = new ArrayList<>();
    private BackgroundParticles particles = new BackgroundParticles();
    private NotificationManager notifications = new NotificationManager();

    public ConfigScreen(Screen parent) {
        super(Component.literal("Config Manager"));
        this.parent = parent;
        ConfigManager.init();
        reloadConfigs();
    }

    private void reloadConfigs() {
        this.configs = ConfigManager.listConfigs();
        // Пересчитываем скролл
        int contentHeight = configs.size() * 25;
        this.maxScroll = Math.max(0, contentHeight - (windowHeight - 60));
    }

    @Override
    protected void init() {
        if (!initialized) {
            // Центрируем окно
            this.windowX = (this.width - this.windowWidth) / 2;
            this.windowY = (this.height - this.windowHeight) / 2;
            initialized = true;
        }

        // Инициализация поля ввода
        int inputWidth = 180;
        nameInput = new ModernTextBox(windowX + windowWidth - inputWidth - 20, windowY + 60, inputWidth, 22, "New Config Name...");

        // Инициализация кнопок действий (Справа)
        actionButtons.clear();
        int btnX = windowX + windowWidth - inputWidth - 20;
        int startY = windowY + 100;

        // Кнопка CREATE/SAVE (Y = +100)
        actionButtons.add(new ModernButton("Create / Save", btnX, startY, inputWidth, 25, () -> {
            String name = nameInput.getText();
            if (name.isEmpty() && selectedConfig != null) name = selectedConfig;

            if (!name.isEmpty()) {
                ConfigManager.saveConfig(name);
                notifications.add("Saved config: " + name, 0xFF55FF55);
                nameInput.setText("");
                reloadConfigs();
            } else {
                notifications.add("Error: Name is empty!", 0xFFFF5555);
            }
        }));

        // Кнопка LOAD (Y = +135)
        actionButtons.add(new ModernButton("Load Selected", btnX, startY + 35, inputWidth, 25, () -> {
            if (selectedConfig != null) {
                ConfigManager.loadConfig(selectedConfig);
                notifications.add("Loaded: " + selectedConfig, 0xFF55FFFF);
            } else {
                notifications.add("Select a config first!", 0xFFFF5555);
            }
        }));

        // Кнопка DELETE (Y = +170, высота 25 -> заканчивается на +195)
        actionButtons.add(new ModernButton("Delete Selected", btnX, startY + 70, inputWidth, 25, () -> {
            if (selectedConfig != null) {
                ConfigManager.deleteConfig(selectedConfig);
                notifications.add("Deleted: " + selectedConfig, 0xFFAAAAAA);
                selectedConfig = null;
                reloadConfigs();
            } else {
                notifications.add("Select a config first!", 0xFFFF5555);
            }
        }));

        // Кнопка BACK (Внизу)
        actionButtons.add(new ModernButton("Back to GUI", btnX, windowY + windowHeight - 40, inputWidth, 25, () -> {
            this.minecraft.setScreen(parent);
        }));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        // 1. Анимация скролла
        scrollOffset = lerp(scrollOffset, targetScroll, partialTick * 0.2f);
        if (Math.abs(scrollOffset - targetScroll) < 0.1) scrollOffset = targetScroll;

        // 2. Фон экрана
        this.renderBackground(context, mouseX, mouseY, partialTick);

        // 3. Частицы
        particles.updateAndRender(context, this.width, this.height, mouseX, mouseY);

        // --- ГЛАВНОЕ ОКНО ---
        drawRoundedRect(context, windowX - 4, windowY - 4, windowWidth + 8, windowHeight + 8, 14, new Color(0, 0, 0, 100).getRGB());
        drawRoundedRect(context, windowX - 1, windowY - 1, windowWidth + 2, windowHeight + 2, 12, new Color(64, 224, 208).getRGB());
        drawRoundedRect(context, windowX, windowY, windowWidth, windowHeight, 12, new Color(20, 20, 25, 250).getRGB());

        // --- ЗАГОЛОВОК ---
        context.drawString(this.font, "Config Manager", windowX + 20, windowY + 20, 0xFFFFFF, true);
        context.fill(windowX + 20, windowY + 35, windowX + 150, windowY + 36, 0xFF40E0D0);

        // --- ЛЕВАЯ ПАНЕЛЬ (СПИСОК) ---
        int listX = windowX + 20;
        int listY = windowY + 50;
        int listW = 200;
        int listH = windowHeight - 70;

        drawRoundedRect(context, listX, listY, listW, listH, 6, new Color(15, 15, 20).getRGB());

        // SCISSOR TEST
        context.enableScissor(listX, listY, listX + listW, listY + listH);

        int itemY = listY + 5 - (int)scrollOffset;

        for (String cfgName : configs) {
            if (itemY > windowY && itemY < windowY + windowHeight) {
                boolean isSelected = cfgName.equals(selectedConfig);
                boolean isHovered = mouseX >= listX + 5 && mouseX <= listX + listW - 5 && mouseY >= itemY && mouseY <= itemY + 20;

                int itemColor = isSelected ? new Color(40, 40, 60).getRGB() : (isHovered ? new Color(30, 30, 40).getRGB() : new Color(0,0,0,0).getRGB());

                drawRoundedRect(context, listX + 5, itemY, listW - 10, 20, 4, itemColor);

                if (isSelected) {
                    context.fill(listX + 5, itemY + 2, listX + 7, itemY + 18, 0xFF40E0D0);
                }

                int textColor = isSelected ? 0xFFFFFF : 0xAAAAAA;
                context.drawString(this.font, cfgName, listX + 15, itemY + 6, textColor, false);
            }
            itemY += 25;
        }

        context.disableScissor();

        // --- ПРАВАЯ ПАНЕЛЬ (ДЕЙСТВИЯ) ---
        nameInput.render(context, mouseX, mouseY);

        for (ModernButton btn : actionButtons) {
            btn.render(context, mouseX, mouseY);
        }

        // Инфо-текст (ИСПРАВЛЕНО ПОЛОЖЕНИЕ)
        // Сместили ниже (на 210), теперь не перекрывает кнопку Delete
        if (selectedConfig != null) {
            String txt = "Selected: " + selectedConfig;
            context.drawCenteredString(this.font, txt, windowX + windowWidth - 110, windowY + 210, 0xFFAAAAAA);
        }

        // --- УВЕДОМЛЕНИЯ ---
        notifications.render(context, windowX + windowWidth - 10, windowY + windowHeight - 10);
    }

    // --- INPUT HANDLING ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        nameInput.mouseClicked(mouseX, mouseY, button);

        for (ModernButton btn : actionButtons) {
            if (btn.mouseClicked(mouseX, mouseY, button)) {
                playClick();
                return true;
            }
        }

        int listX = windowX + 20;
        int listY = windowY + 50;
        int listW = 200;
        int listH = windowHeight - 70;

        if (mouseX >= listX && mouseX <= listX + listW && mouseY >= listY && mouseY <= listY + listH) {
            int itemY = listY + 5 - (int)scrollOffset;
            for (String cfgName : configs) {
                if (mouseY >= itemY && mouseY <= itemY + 20) {
                    selectedConfig = cfgName;
                    playClick();
                    return true;
                }
                itemY += 25;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            targetScroll -= scrollY * 20;
            if (targetScroll < 0) targetScroll = 0;
            if (targetScroll > maxScroll) targetScroll = maxScroll;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (nameInput.charTyped(codePoint, modifiers)) return true;
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (nameInput.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void playClick() {
        Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    // --- VISUAL UTILS ---

    private void drawRoundedRect(GuiGraphics context, int x, int y, int width, int height, int radius, int color) {
        context.fill(x + radius, y, x + width - radius, y + height, color);
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        context.fill(x + 1, y + 1, x + radius, y + radius, color);
        context.fill(x + width - radius, y + 1, x + width - 1, y + radius, color);
        context.fill(x + width - radius, y + height - radius, x + width - 1, y + height - 1, color);
        context.fill(x + 1, y + height - radius, x + radius, y + height - 1, color);
    }

    // ==========================================
    // INNER CLASSES (COMPONENTS)
    // ==========================================

    private class ModernTextBox {
        int x, y, width, height;
        String text = "";
        String placeholder;
        boolean isFocused = false;
        int cursorTimer = 0;

        public ModernTextBox(int x, int y, int width, int height, String placeholder) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.placeholder = placeholder;
        }

        public void render(GuiGraphics context, int mouseX, int mouseY) {
            int bgColor = isFocused ? new Color(30, 30, 40).getRGB() : new Color(20, 20, 25).getRGB();
            int borderColor = isFocused ? 0xFF40E0D0 : 0xFF444444;

            drawRoundedRect(context, x - 1, y - 1, width + 2, height + 2, 4, borderColor);
            drawRoundedRect(context, x, y, width, height, 4, bgColor);

            if (text.isEmpty() && !isFocused) {
                context.drawString(font, placeholder, x + 5, y + (height - 8) / 2, 0xFF666666, false);
            } else {
                String renderText = font.plainSubstrByWidth(text, width - 10);
                context.drawString(font, renderText, x + 5, y + (height - 8) / 2, 0xFFFFFFFF, false);
                if (isFocused && (cursorTimer / 10) % 2 == 0) {
                    int txtWidth = font.width(renderText);
                    if (txtWidth < width - 10) {
                        context.fill(x + 5 + txtWidth, y + 4, x + 6 + txtWidth, y + height - 4, 0xFFFFFFFF);
                    }
                }
            }
            cursorTimer++;
        }

        public void setText(String t) { this.text = t; }
        public String getText() { return text; }

        public void mouseClicked(double mx, double my, int btn) {
            boolean hovered = mx >= x && mx <= x + width && my >= y && my <= y + height;
            if (hovered && btn == 0) isFocused = true;
            else if (btn == 0) isFocused = false;
        }

        public boolean charTyped(char codePoint, int modifiers) {
            if (!isFocused) return false;
            if (Character.isLetterOrDigit(codePoint) || codePoint == '_' || codePoint == '-' || codePoint == ' ') {
                text += codePoint;
                return true;
            }
            return false;
        }

        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (!isFocused) return false;
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
                return true;
            }
            return false;
        }
    }

    private class ModernButton {
        String label;
        int x, y, width, height;
        Runnable action;
        float hoverAnim = 0;

        public ModernButton(String label, int x, int y, int width, int height, Runnable action) {
            this.label = label; this.x = x; this.y = y; this.width = width; this.height = height; this.action = action;
        }

        public void render(GuiGraphics context, int mouseX, int mouseY) {
            boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
            if (hovered) { if (hoverAnim < 1.0f) hoverAnim += 0.1f; }
            else { if (hoverAnim > 0.0f) hoverAnim -= 0.1f; }

            int r = (int) (30 + (34 * hoverAnim));
            int g = (int) (30 + (194 * hoverAnim));
            int b = (int) (40 + (168 * hoverAnim));
            int color = (255 << 24) | (r << 16) | (g << 8) | b;

            drawRoundedRect(context, x, y, width, height, 4, color);
            context.drawCenteredString(font, label, x + width / 2, y + (height - 8) / 2, 0xFFFFFF);
        }

        public boolean mouseClicked(double mx, double my, int btn) {
            if (mx >= x && mx <= x + width && my >= y && my <= y + height && btn == 0) {
                action.run();
                return true;
            }
            return false;
        }
    }

    private class BackgroundParticles {
        private final List<Particle> particleList = new ArrayList<>();
        private class Particle {
            float x, y, speedX, speedY, size, alpha;
            public Particle(float w, float h) { reset(w, h); }
            void reset(float w, float h) {
                x = (float) (Math.random() * w); y = (float) (Math.random() * h);
                speedX = (float) ((Math.random() - 0.5) * 0.5); speedY = (float) ((Math.random() - 0.5) * 0.5);
                size = (float) (Math.random() * 2 + 1); alpha = 0;
            }
        }
        public BackgroundParticles() { for (int i = 0; i < 50; i++) particleList.add(new Particle(1000, 1000)); }
        public void updateAndRender(GuiGraphics context, int w, int h, int mx, int my) {
            for (Particle p : particleList) {
                p.x += p.speedX; p.y += p.speedY;
                if (p.alpha < 100) p.alpha += 1;
                if (p.x < 0 || p.x > w || p.y < 0 || p.y > h) p.reset(w, h);
                float moveX = (mx - w/2) * 0.02f; float moveY = (my - h/2) * 0.02f;
                int color = ((int)p.alpha << 24) | (64 << 16) | (224 << 8) | 208;
                context.fill((int)(p.x + moveX), (int)(p.y + moveY), (int)(p.x + moveX + p.size), (int)(p.y + moveY + p.size), color);
            }
        }
    }

    private class NotificationManager {
        private class Notification {
            String text; int color; long spawnTime;
            public Notification(String text, int color) { this.text = text; this.color = color; this.spawnTime = System.currentTimeMillis(); }
        }
        private final List<Notification> notes = new ArrayList<>();
        public void add(String text, int color) { notes.add(new Notification(text, color)); }
        public void render(GuiGraphics context, int startX, int startY) {
            long now = System.currentTimeMillis();
            notes.removeIf(n -> now - n.spawnTime > 2000);
            int y = startY;
            for (Notification n : notes) {
                float life = (now - n.spawnTime) / 2000.0f;
                float alpha = 1.0f; if (life > 0.8f) alpha = 1.0f - ((life - 0.8f) * 5.0f);
                int width = font.width(n.text) + 10; int x = startX - width;
                int bg = ((int)(alpha * 200) << 24) | (0 << 16) | (0 << 8) | 0;
                drawRoundedRect(context, x, y - 20, width, 18, 4, bg);
                context.drawString(font, n.text, x + 5, y - 15, n.color, false);
                y -= 25;
            }
        }
    }
}