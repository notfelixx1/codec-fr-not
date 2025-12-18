package nott.notarmy.Combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class TriggerBot {

    public static boolean isEnabled = false;
    public static boolean onlyCrits = true; // Бить только когда мы падаем (крит)

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // 1. Проверяем кулдаун (заряд меча)
        // Если меч не заряжен на 100%, не бьем (иначе урон будет смешной)
        if (mc.player.getAttackStrengthScale(0.0f) < 1.0f) return;

        // 2. Если включен режим "Только криты" - проверяем падение
        // Мы должны падать, не быть на земле, не в воде, не на лестнице
        if (onlyCrits) {
            boolean isFalling = mc.player.getDeltaMovement().y < 0;
            if (!isFalling || mc.player.onGround() || mc.player.isInWater() || mc.player.onClimbable()) {
                return;
            }
        }

        // 3. Проверяем, на что смотрим
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) mc.hitResult).getEntity();

            // Бьем только живых и только врагов (не себя)
            if (target instanceof LivingEntity && target != mc.player) {
                // Если это игрок или монстр (можно добавить фильтры)
                if (target instanceof Player || target instanceof net.minecraft.world.entity.monster.Monster) {

                    // БЬЕМ!
                    mc.gameMode.attack(mc.player, target);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }
}