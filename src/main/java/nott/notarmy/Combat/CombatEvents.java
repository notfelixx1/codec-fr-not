package nott.notarmy.Combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class CombatEvents {

    public static boolean isAutoAxeEnabled = false;

    // Таймер, чтобы не переключать оружие слишком часто (защита от античита)
    private static int cooldown = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (cooldown > 0) cooldown--;
    }

    // Срабатывает в момент нажатия кнопки атаки (ЛКМ)
    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!isAutoAxeEnabled || !event.isAttack() || event.getKeyMapping().isUnbound()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (cooldown > 0) return;

        // Проверяем, на кого смотрим
        HitResult result = mc.hitResult;
        if (result != null && result.getType() == HitResult.Type.ENTITY) {
            Entity targetRaw = ((EntityHitResult) result).getEntity();

            if (targetRaw instanceof LivingEntity target) {
                int bestSlot = -1;

                // --- СЦЕНАРИЙ 1: ВРАГ В ЩИТЕ (Нужен топор) ---
                if (isBlockingWithShield(target)) {
                    bestSlot = findBestAxe(mc.player);
                }
                // --- СЦЕНАРИЙ 2: ОБЫЧНАЯ АТАКА (Нужен макс. урон) ---
                else {
                    bestSlot = findBestWeapon(mc.player, target);
                }

                // Если нашли слот лучше текущего -> переключаем
                if (bestSlot != -1 && bestSlot != mc.player.getInventory().selected) {
                    mc.player.getInventory().selected = bestSlot;
                    cooldown = 2; // Небольшая задержка перед следующим свитчем
                }
            }
        }
    }

    // Проверка: Держит ли враг активный щит
    private static boolean isBlockingWithShield(LivingEntity target) {
        if (target.isBlocking()) {
            // Проверяем, что в руках именно щит (а не просто он ест еду или натягивает лук)
            return target.getMainHandItem().getItem().toString().contains("shield") ||
                    target.getOffhandItem().getItem().toString().contains("shield");
        }
        return false;
    }

    // Поиск лучшего топора (для пробития щита)
    private static int findBestAxe(Player player) {
        int bestSlot = -1;
        float bestDamage = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() instanceof AxeItem) {
                // Считаем урон топора (база + чары)
                float damage = getToolDamage(stack);
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    // Поиск оружия с самым большим уроном (Меч или Топор)
    private static int findBestWeapon(Player player, LivingEntity target) {
        int bestSlot = -1;
        float bestDamage = -1;

        // Получаем урон текущего предмета, чтобы сравнить
        ItemStack currentStack = player.getMainHandItem();
        bestDamage = getToolDamage(currentStack);
        bestSlot = player.getInventory().selected;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            // Проверяем только оружие (Мечи и Топоры)
            if (stack.getItem() instanceof SwordItem || stack.getItem() instanceof AxeItem) {
                float damage = getToolDamage(stack);

                // Добавляем бонус от чар (Острота, Небесная кара и т.д.) конкретно по этому мобу
                // (В 1.20+ это делается немного иначе, но базовый метод работает)
                // float enchantBonus = EnchantmentHelper.getDamageBonus(stack, target.getMobType());
                // damage += enchantBonus;

                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    // Универсальный метод расчета чистого урона оружия
    private static float getToolDamage(ItemStack stack) {
        // Получаем атрибут урона. По умолчанию кулак = 1.
        // Multimap атрибутов сложный, поэтому берем упрощенно через NBT или базовый класс,
        // но самый надежный способ в API - пройтись по модификаторам.

        // Простой способ для 1.20.4:
        double damage = 0;
        try {
            // Получаем базовый урон предмета
            var attributes = stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            if (attributes.containsKey(Attributes.ATTACK_DAMAGE)) {
                damage = attributes.get(Attributes.ATTACK_DAMAGE).stream()
                        .mapToDouble(net.minecraft.world.entity.ai.attributes.AttributeModifier::getAmount)
                        .sum();
            }
            // Добавляем базовый урон игрока (обычно 1.0)
            damage += 1.0;

            // Добавляем уровень чар "Острота" (Sharpness) грубо: 0.5 * уровень + 0.5
            int sharpLevel = stack.getEnchantmentLevel(net.minecraft.world.item.enchantment.Enchantments.SHARPNESS);
            if (sharpLevel > 0) {
                damage += 0.5 * sharpLevel + 0.5;
            }
        } catch (Exception e) {
            return 1.0f;
        }

        return (float) damage;
    }
}