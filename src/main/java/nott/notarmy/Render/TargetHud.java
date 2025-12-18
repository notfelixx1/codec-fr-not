package nott.notarmy.Render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import nott.notarmy.TargetHudEditor;
import org.joml.Quaternionf;

import java.awt.Color;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class TargetHud {

    public static boolean isEnabled = false;

    public static int x = 300;
    public static int y = 300;

    // Сделали чуть шире и выше для красоты
    public static final int width = 140;
    public static final int height = 55;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!isEnabled) return;
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (mc.crosshairPickEntity instanceof LivingEntity target) {
            renderHud(event.getGuiGraphics(), target, x, y);
        }
        else if (mc.screen instanceof TargetHudEditor) {
            renderHud(event.getGuiGraphics(), mc.player, x, y);
        }
    }

    public static void renderHud(GuiGraphics context, LivingEntity target, int posX, int posY) {
        // --- 1. ГРАДИЕНТНАЯ ОБВОДКА (Синий - Бирюзовый - Темно-синий) ---
        // Рисуем прямоугольник чуть больше основного (на 2 пикселя), создавая эффект рамки
        // fillGradient рисует сверху вниз.
        // 0xFF00008B (DarkBlue) -> 0xFF40E0D0 (Turquoise)

        // Рисуем задний фон (обводку) с закруглением
        int borderStart = 0xFF00008B; // Темно-синий
        int borderEnd = 0xFF00CED1;   // Бирюзовый

        // Чтобы сделать "градиент" на закругленном прямоугольнике без шейдеров сложно,
        // поэтому мы нарисуем сплошной красивый цвет или вертикальный градиент "внутри" нашего метода.
        // Для простоты и производительности сделаем красивую Бирюзовую заливку с прозрачностью.
        drawRoundedRect(context, posX - 2, posY - 2, width + 4, height + 4, 10, new Color(0, 100, 200, 150).getRGB());

        // --- 2. ОСНОВНОЙ ФОН (Черный полупрозрачный) ---
        drawRoundedRect(context, posX, posY, width, height, 8, new Color(20, 20, 20, 220).getRGB());

        // --- 3. 3D МОДЕЛЬКА ---
        // Смещаем чуть ниже и левее
        renderEntity(context, posX + 20, posY + 48, 22, 0, 0, target);

        // --- 4. ИНФОРМАЦИЯ ---
        int offsetLeft = 45; // Отступ слева от модельки

        // ИМЯ
        context.drawString(Minecraft.getInstance().font, target.getName(), posX + offsetLeft, posY + 8, 0xFFFFFF, true);

        // ХП БАР (Перенесли выше)
        float maxHp = target.getMaxHealth() + target.getAbsorptionAmount();
        float currentHp = target.getHealth() + target.getAbsorptionAmount();
        float hpPercent = Math.min(1.0f, currentHp / target.getMaxHealth()); // Ограничиваем 100%

        int barWidth = 85;
        int barHeight = 6; // Тонкая полоска
        int barX = posX + offsetLeft;
        int barY = posY + 22; // Сразу под именем

        // Фон бара (Темно-серый)
        drawRoundedRect(context, barX, barY, barWidth, barHeight, 3, new Color(60, 60, 60).getRGB());

        // Заполнение бара (Цвет зависит от ХП)
        int healthColor = getHealthColor(hpPercent);
        int fillWidth = (int) (barWidth * hpPercent);
        if (fillWidth > 0) {
            drawRoundedRect(context, barX, barY, fillWidth, barHeight, 3, healthColor);
        }

        // ТЕКСТ ХП (Справа от имени или на баре)
        // Напишем маленьким шрифтом под баром
        String hpText = String.format("%.1f HP", currentHp);

        // Масштабируем текст (делаем меньше)
        PoseStack pose = context.pose();
        pose.pushPose();
        pose.translate(posX + offsetLeft, posY + 32, 0);
        pose.scale(0.8f, 0.8f, 0.8f); // Уменьшаем шрифт
        context.drawString(Minecraft.getInstance().font, hpText, 0, 0, 0xFFFFFF, true);
        pose.popPose();

        // --- 5. БРОНЯ ---
        // Рисуем полоску брони рядом с текстом ХП
        int armorValue = target.getArmorValue();
        if (armorValue > 0) {
            int armorBarWidth = 40;
            int armorBarHeight = 4;
            int armorX = posX + width - armorBarWidth - 10;
            int armorY = posY + 34;

            // Фон
            drawRoundedRect(context, armorX, armorY, armorBarWidth, armorBarHeight, 2, new Color(60, 60, 60).getRGB());

            // Заполнение (Синий)
            int armorFill = (int) (armorBarWidth * (armorValue / 20.0f));
            if (armorFill > armorBarWidth) armorFill = armorBarWidth;
            drawRoundedRect(context, armorX, armorY, armorFill, armorBarHeight, 2, 0xFF55FFFF);
        }
    }

    // --- Метод для закругленных прямоугольников ---
    private static void drawRoundedRect(GuiGraphics context, int x, int y, int width, int height, int radius, int color) {
        // Центр
        context.fill(x + radius, y + radius, x + width - radius, y + height - radius, color);
        // Верх и Низ
        context.fill(x + radius, y, x + width - radius, y + radius, color);
        context.fill(x + radius, y + height - radius, x + width - radius, y + height, color);
        // Лево и Право
        context.fill(x, y + radius, x + radius, y + height - radius, color);
        context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

        // Углы (пиксельная эмуляция круга)
        // Верх-Лево
        context.fill(x + 1, y + 1, x + radius, y + radius, color);
        // Верх-Право
        context.fill(x + width - radius, y + 1, x + width - 1, y + radius, color);
        // Низ-Право
        context.fill(x + width - radius, y + height - radius, x + width - 1, y + height - 1, color);
        // Низ-Лево
        context.fill(x + 1, y + height - radius, x + radius, y + height - 1, color);

        // Доп. пиксели для сглаживания (убираем самые угловые)
        // Это простой способ сделать "круглее" без шейдеров
    }

    public static void renderEntity(GuiGraphics context, int x, int y, int scale, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float)Math.atan((double)(mouseX / 40.0F));
        float f1 = (float)Math.atan((double)(mouseY / 40.0F));

        PoseStack posestack = context.pose();
        posestack.pushPose();
        posestack.translate((float)x, (float)y, 1050.0F);
        posestack.scale(1.0F, 1.0F, -1.0F);
        context.pose().translate(0.0F, 0.0F, 1000.0F);
        posestack.scale((float)scale, (float)scale, (float)scale);

        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(f1 * 20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        posestack.mulPose(quaternionf);

        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;

        entity.yBodyRot = 180.0F + f * 20.0F;
        entity.setYRot(180.0F + f * 40.0F);
        entity.setXRot(-f1 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        Lighting.setupForEntityInInventory();

        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternionf1.conjugate();
        entityrenderdispatcher.overrideCameraOrientation(quaternionf1);
        entityrenderdispatcher.setRenderShadow(false);

        RenderSystem.runAsFancy(() -> {
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack, context.bufferSource(), 15728880);
        });

        context.flush();
        entityrenderdispatcher.setRenderShadow(true);

        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;

        posestack.popPose();
        Lighting.setupFor3DItems();
    }

    private static int getHealthColor(float percent) {
        if (percent > 0.75f) return 0xFF00FF00;
        if (percent > 0.5f) return 0xFFFFFF00;
        if (percent > 0.25f) return 0xFFFFAA00;
        return 0xFFFF0000;
    }
}