package nott.notarmy.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class Tracers {

    public static boolean isEnabled = false;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        Matrix4f matrix = poseStack.last().pose();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // Подготовка рендера
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // Видим сквозь стены
        RenderSystem.disableCull();
        RenderSystem.lineWidth(1.5f); // Толщина линии

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Вектор взгляда игрока (откуда идет линия)
        Vec3 lookVec = mc.player.getViewVector(event.getPartialTick()).scale(100);
        // Но лучше рисовать просто от глаз (камеры)

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player target && target != mc.player) {

                // Интерполяция позиции (для плавности)
                double x = androidLerp(target.xo, target.getX(), event.getPartialTick()) - cameraPos.x;
                double y = androidLerp(target.yo, target.getY(), event.getPartialTick()) - cameraPos.y;
                double z = androidLerp(target.zo, target.getZ(), event.getPartialTick()) - cameraPos.z;

                // Цвет линии (зависит от дистанции: близко - красный, далеко - зеленый)
                float dist = mc.player.distanceTo(target);
                int color = getColorByDist(dist);
                int r = (color >> 16) & 0xFF;
                int g = (color >> 8) & 0xFF;
                int b = color & 0xFF;

                // Рисуем линию от глаз (0, 0 + eyeHeight, 0) к центру игрока
                buffer.vertex(matrix, 0, (float) mc.player.getEyeHeight(), 0)
                        .color(r, g, b, 255).endVertex();

                buffer.vertex(matrix, (float) x, (float) y + target.getEyeHeight() / 2, (float) z)
                        .color(r, g, b, 255).endVertex();
            }
        }

        tesselator.end();

        // Сброс
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
    }

    private static double androidLerp(double start, double end, float delta) {
        return start + (end - start) * delta;
    }

    private static int getColorByDist(float dist) {
        if (dist < 10) return 0xFF0000; // Красный (Опасно)
        if (dist < 30) return 0xFFA500; // Оранжевый
        return 0x00FF00;                // Зеленый (Далеко)
    }
}