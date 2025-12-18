package nott.notarmy.Render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class ChestEsp {

    public static boolean isChestEspEnabled = false;

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!isChestEspEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        Vec3 cameraPos = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // --- НАСТРОЙКА РЕНДЕРА (Самое важное) ---
        // 1. Включаем шейдер для цветных линий
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        // 2. Включаем прозрачность (чтобы линии были гладкими)
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // 3. ОТКЛЮЧАЕМ ГЛУБИНУ (WallHack) - Линии будут рисоваться поверх всего
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        // 4. Отключаем отсечение (чтобы видеть линии с любой стороны)
        RenderSystem.disableCull();
        // 5. Толщина линии (может не работать на некоторых драйверах Nvidia, но попробуем)
        RenderSystem.lineWidth(2.0f);

        // --- ПРЯМОЙ ДОСТУП К TESSELATOR ---
        // Мы не используем bufferSource, мы рисуем "вручную" здесь и сейчас
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        // Начинаем рисовать ЛИНИИ
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Поиск сундуков по чанкам (этот метод у нас работал хорошо)
        ChunkPos chunkPos = mc.player.chunkPosition();
        int renderDistance = mc.options.getEffectiveRenderDistance();

        for (int x = chunkPos.x - renderDistance; x <= chunkPos.x + renderDistance; x++) {
            for (int z = chunkPos.z - renderDistance; z <= chunkPos.z + renderDistance; z++) {
                if (mc.level.hasChunk(x, z)) {
                    LevelChunk chunk = mc.level.getChunk(x, z);
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be.isRemoved()) continue;

                        boolean isChest = be instanceof ChestBlockEntity;
                        boolean isEnder = be instanceof EnderChestBlockEntity;
                        boolean isTrapped = be instanceof TrappedChestBlockEntity;

                        if (isChest || isEnder || isTrapped) {
                            BlockPos pos = be.getBlockPos();

                            float r = 1.0f, g = 1.0f, b = 0.0f; // Желтый
                            if (isEnder) { r = 0.8f; g = 0.0f; b = 1.0f; } // Фиолетовый
                            if (isTrapped) { r = 1.0f; g = 0.0f; b = 0.0f; } // Красный

                            // Рисуем коробку
                            // Используем renderLineBox, но передаем наш ручной буфер
                            AABB aabb = new AABB(pos);
                            LevelRenderer.renderLineBox(poseStack, buffer, aabb, r, g, b, 1.0f);
                        }
                    }
                }
            }
        }

        // Заканчиваем рисование и отправляем на видеокарту
        tesselator.end();

        // --- ВОССТАНАВЛИВАЕМ НАСТРОЙКИ ---
        // Очень важно вернуть всё как было, иначе весь мир станет прозрачным
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);

        poseStack.popPose();
    }
}