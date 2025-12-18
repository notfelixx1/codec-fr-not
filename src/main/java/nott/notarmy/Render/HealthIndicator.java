package nott.notarmy.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class HealthIndicator {

    public static boolean isHpEnabled = false;

    // Используем RenderLevelStageEvent, чтобы рисовать в мире, а не в GUI
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!isHpEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        Font font = mc.font;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        // Проходим по всем сущностям в мире
        for (Entity entity : mc.level.entitiesForRendering()) {

            // Фильтры: Живое существо, не АрморСтенд, не мы сами
            if (!(entity instanceof LivingEntity livingEntity)) continue;
            if (entity instanceof ArmorStand) continue;
            if (entity == mc.player) continue;

            // Оптимизация: не рендерим, если моб дальше 100 блоков (чтобы не лагало)
            if (entity.distanceTo(mc.player) > 40) continue;

            // --- РАСЧЕТ ЗДОРОВЬЯ ---
            float hp = livingEntity.getHealth() + livingEntity.getAbsorptionAmount();
            float maxHp = livingEntity.getMaxHealth();
            if (maxHp <= 0) maxHp = 1;
            float percent = hp / maxHp;

            // Цвет
            int color;
            if (percent > 0.75) color = 0xFF00FF00;      // Зеленый
            else if (percent > 0.50) color = 0xFFFFFF00; // Желтый
            else if (percent > 0.25) color = 0xFFFFAA00; // Оранжевый
            else color = 0xFFFF0000;                     // Красный

            String text = String.valueOf((int) Math.ceil(hp)) + " hp";

            // --- ОТРИСОВКА ---
            poseStack.pushPose();

            // 1. Смещаемся к позиции моба
            // Используем линейную интерполяцию для плавности (чтобы текст не дергался)
            double x = androidLerp(entity.xo, entity.getX(), event.getPartialTick()) - cameraPos.x;
            double y = androidLerp(entity.yo, entity.getY(), event.getPartialTick()) - cameraPos.y;
            double z = androidLerp(entity.zo, entity.getZ(), event.getPartialTick()) - cameraPos.z;

            // Поднимаем текст над головой (высота моба + 0.5 блока)
            poseStack.translate(x, y + entity.getBbHeight() + 0.5f, z);

            // 2. Поворачиваем текст к игроку (Billboard эффект)
            // Берем поворот камеры и применяем к тексту
            poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());

            // 3. Масштабируем (текст в мире огромный, нужно уменьшить)
            // -0.025f переворачивает текст по Y, так как в OpenGL координаты перевернуты
            float scale = 0.025f;
            poseStack.scale(-scale, -scale, scale);

            Matrix4f matrix = poseStack.last().pose();

            // Центрируем текст
            float textWidth = font.width(text);
            float textX = -textWidth / 2;

            // 4. ГЛАВНОЕ: Отключаем тест глубины (WallHack)
            RenderSystem.disableDepthTest();

            // Рисуем фон (полупрозрачный черный) для читаемости
            int backgroundColor = 0x40000000; // Черный с прозрачностью
            font.drawInBatch(text, textX, 0, color, false, matrix, bufferSource, Font.DisplayMode.SEE_THROUGH, backgroundColor, 0xF000F0);

            // Рисуем сам текст (обычный) поверх
            font.drawInBatch(text, textX, 0, color, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 0xF000F0);

            // Включаем глубину обратно, чтобы не сломать остальной мир
            RenderSystem.enableDepthTest();

            poseStack.popPose();
        }

        // Завершаем отрисовку
        bufferSource.endBatch();
    }

    // Вспомогательный метод для интерполяции (плавного движения)
    private static double androidLerp(double start, double end, float delta) {
        return start + (end - start) * delta;
    }
}