package nott.notarmy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;

public class CheckboxComponent extends ComponentBase {

    private final ModuleManager.BooleanSetting setting;

    public CheckboxComponent(ModuleManager.BooleanSetting s) {
        this.setting = s;
    }

    @Override
    public void render(GuiGraphics context, double x, double y, double w, int mx, int my) {
        RenderUtils.drawRect(context, x, y, w, 14, 0xFF151515);
        context.drawString(Minecraft.getInstance().font, setting.name, (int)x + 4, (int)y + 3, 0xFFCCCCCC, false);

        double boxSize = 10;
        double boxX = x + w - 14;
        double boxY = y + 2;

        RenderUtils.drawOutline(context, boxX, boxY, boxSize, boxSize, 1, 0xFF505050);

        if (setting.getter.get()) {
            RenderUtils.drawRect(context, boxX + 2, boxY + 2, boxSize - 4, boxSize - 4, 0xFF0090FF);
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, double w, double mx, double my, int btn) {
        if (mx >= x && mx <= x + w && my >= y && my <= y + 14 && btn == 0) {
            setting.toggle();
            Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }

    @Override
    public double getHeight() {
        return 14;
    }
}
