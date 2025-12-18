package nott.notarmy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.world.entity.Entity;
import nott.notarmy.Combat.Velocity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class MixinClientPacketListener {

    // --- ОБРАБОТКА УДАРОВ (Perfect Legit Logic) ---
    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true, remap = false)
    private void onPacketVelocity(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        try {
            if (Velocity.isEnabled && Minecraft.getInstance().player != null) {
                if (packet.getId() == Minecraft.getInstance().player.getId()) {

                    // 1. ПРОВЕРКА ШАНСА (Legit Mode)
                    // Если выпало число больше шанса -> пропускаем удар (нас откинет нормально)
                    // Это сбивает детект античита.
                    if (Velocity.chance < 100.0) {
                        if (Math.random() * 100 > Velocity.chance) {
                            return; // Не вмешиваемся
                        }
                    }

                    // Отменяем стандартную обработку пакета (чтобы игра не сделала это сама)
                    ci.cancel();

                    Entity player = Minecraft.getInstance().player;

                    // Переводим пакетные данные в реальную скорость
                    double packetX = packet.getXa() / 8000.0D;
                    double packetY = packet.getYa() / 8000.0D;
                    double packetZ = packet.getZa() / 8000.0D;

                    // 2. ПРИМЕНЯЕМ НАШИ МНОЖИТЕЛИ
                    double newX = packetX * Velocity.horizontal;
                    double newY = packetY * Velocity.vertical;
                    double newZ = packetZ * Velocity.horizontal;

                    // 3. ТЕХНОЛОГИЯ "MICRO-PUSH" (Обход Grim/Vulcan)
                    // Если velocity выставлено в 0, мы добавляем ничтожное значение.
                    // Сервер видит, что скорость изменилась (не 0), и не флагает "Ignored Velocity".
                    if (Velocity.horizontal == 0) {
                        newX = (Math.random() - 0.5) * 0.0005; // Крошечный шум
                        newZ = (Math.random() - 0.5) * 0.0005;
                    }

                    // Применяем итоговую скорость
                    player.setDeltaMovement(newX, newY, newZ);
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки, чтобы не крашнуть игру в бою
        }
    }

    // --- ОБРАБОТКА ВЗРЫВОВ ---
    // require = 0 спасает от краша, если метод не найден в этой версии
    @Inject(method = {"handleExplosion", "handleExplode"}, at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void onPacketExplode(ClientboundExplodePacket packet, CallbackInfo ci) {
        if (Velocity.isEnabled && Minecraft.getInstance().player != null) {

            // То же самое для взрывов
            if (Velocity.chance < 100.0 && Math.random() * 100 > Velocity.chance) return;

            ci.cancel();

            Entity player = Minecraft.getInstance().player;

            double kbX = packet.getKnockbackX() * Velocity.horizontal;
            double kbY = packet.getKnockbackY() * Velocity.vertical;
            double kbZ = packet.getKnockbackZ() * Velocity.horizontal;

            // Micro-Push для взрывов
            if (Velocity.horizontal == 0) {
                kbX = (Math.random() - 0.5) * 0.0005;
                kbZ = (Math.random() - 0.5) * 0.0005;
            }

            // Взрывы в майнкрафте ДОБАВЛЯЮТ скорость к текущей
            player.setDeltaMovement(
                    player.getDeltaMovement().x + kbX,
                    player.getDeltaMovement().y + kbY,
                    player.getDeltaMovement().z + kbZ
            );
        }
    }
}