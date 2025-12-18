package nott.notarmy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import nott.notarmy.Combat.Velocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class MixinEntity {

    // 1. ЖЕЛЕЗОБЕТОННЫЙ KEEP SPRINT
    // Мы перехватываем САМУ установку флага спринта.
    @Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true, remap = false)
    public void onSetSprinting(boolean sprinting, CallbackInfo ci) {
        // Проверяем, что это МЫ (LocalPlayer)
        Entity self = (Entity) (Object) this;
        if (self.level().isClientSide && self == Minecraft.getInstance().player) {

            // Если Velocity включено и игра пытается ВЫКЛЮЧИТЬ спринт (sprinting = false)
            if (Velocity.isEnabled && !sprinting) {

                // Проверяем, что мы вообще двигаемся (чтобы не бежать на месте)
                // Если мы жмем кнопки движения - ЗАПРЕЩАЕМ выключать спринт.
                if (Minecraft.getInstance().player.input.hasForwardImpulse()) {
                    ci.cancel(); // Отменяем выключение. Спринт остается true.
                }
            }
        }
    }

    // 2. АНТИ-ТОЛКАНИЕ (От воды, поршней и мобов)
    // Метод push(x, y, z) отвечает за физическое смещение
    @Inject(method = "push(DDD)V", at = @At("HEAD"), cancellable = true, remap = false)
    public void onPush(double x, double y, double z, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self == Minecraft.getInstance().player && Velocity.isEnabled) {
            // Если стоит 0% Velocity - мы становимся скалой.
            if (Velocity.horizontal == 0 && Velocity.vertical == 0) {
                ci.cancel(); // Полностью отменяем толкание
            }
        }
    }
}