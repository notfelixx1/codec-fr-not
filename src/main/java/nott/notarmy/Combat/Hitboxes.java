package nott.notarmy.Combat;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class Hitboxes {

    public static boolean isEnabled = false;
    public static double expand = 0.5;

    // --- 1. ЛОГИКА УДАРА (MouseButton.Pre - самый ранний перехват) ---
    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        // Работаем только по нажатию ЛКМ (0)
        if (!isEnabled || event.getButton() != GLFW.GLFW_MOUSE_BUTTON_1 || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Если мы в меню - не бьем
        if (mc.screen != null) return;

        // Если игра и так видит СУЩНОСТЬ под прицелом - пусть бьет сама
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            return;
        }

        // --- РУЧНОЙ ПОИСК ВРАГА ---
        double reach = mc.gameMode.getPickRange(); // Дистанция
        Vec3 eyePos = mc.player.getEyePosition(1.0f);
        Vec3 viewVec = mc.player.getViewVector(1.0f);
        Vec3 endPos = eyePos.add(viewVec.scale(reach));

        AABB searchBox = mc.player.getBoundingBox().expandTowards(viewVec.scale(reach)).inflate(1.0);

        // Ищем только игроков
        List<Entity> entities = mc.level.getEntities(mc.player, searchBox,
                e -> e instanceof Player && !e.isSpectator() && e.isPickable() && e != mc.player);

        double closestDist = reach * reach;

        // Если мы смотрим на блок, запоминаем дистанцию до блока
        // Чтобы не бить игрока СКВОЗЬ стену (если стена ближе игрока)
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            closestDist = mc.hitResult.getLocation().distanceToSqr(eyePos);
        }

        Entity bestTarget = null;

        for (Entity entity : entities) {
            // Расширяем хитбокс
            AABB expandedBox = entity.getBoundingBox().inflate(expand);

            Optional<Vec3> hit = expandedBox.clip(eyePos, endPos);

            if (hit.isPresent()) {
                double dist = eyePos.distanceToSqr(hit.get());
                // Если игрок ближе, чем блок, на который мы смотрим (или ближе чем макс дистанция)
                if (dist < closestDist) {
                    closestDist = dist;
                    bestTarget = entity;
                }
            }
        }

        // Если цель найдена
        if (bestTarget != null) {
            // 1. Бьем
            mc.gameMode.attack(mc.player, bestTarget);
            // 2. Машем рукой
            mc.player.swing(InteractionHand.MAIN_HAND);
            // 3. САМОЕ ГЛАВНОЕ: Отменяем событие клика
            // Это запрещает игре обрабатывать "ломание блока" или "удар по воздуху"
            event.setCanceled(true);
        }
    }

    // --- 2. ВИЗУАЛИЗАЦИЯ (Без изменений, только игроки) ---
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (!isEnabled || expand == 0) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.lineWidth(1.5f);

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player && entity != mc.player) {
                double x = androidLerp(entity.xo, entity.getX(), event.getPartialTick());
                double y = androidLerp(entity.yo, entity.getY(), event.getPartialTick());
                double z = androidLerp(entity.zo, entity.getZ(), event.getPartialTick());

                AABB box = entity.getBoundingBox();
                box = box.move(x - entity.getX(), y - entity.getY(), z - entity.getZ());
                box = box.inflate(expand);

                LevelRenderer.renderLineBox(poseStack, buffer, box, 1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        tesselator.end();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.lineWidth(1.0f);
        poseStack.popPose();
    }

    private static double androidLerp(double start, double end, float delta) {
        return start + (end - start) * delta;
    }
}