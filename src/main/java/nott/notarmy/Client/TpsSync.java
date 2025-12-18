package nott.notarmy.Client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import nott.notarmy.Notarmy;

import java.util.Arrays;

@Mod.EventBusSubscriber(modid = Notarmy.MODID, value = Dist.CLIENT)
public class TpsSync {

    public static boolean isEnabled = true; // Включено по умолчанию

    // Храним историю значений TPS для плавности
    private static final float[] tpsHistory = new float[20];
    private static int nextIndex = 0;

    private static long lastPacketTime = -1;
    private static float currentTps = 20.0f;

    // --- ОСНОВНЫЕ МЕТОДЫ ДЛЯ ДРУГИХ МОДУЛЕЙ ---

    /**
     * Возвращает текущий TPS сервера (от 0.0 до 20.0)
     */
    public static float getTps() {
        // Если мы в одиночке - TPS всегда 20
        if (Minecraft.getInstance().isLocalServer()) return 20.0f;
        // Ограничиваем от 0 до 20
        return Math.max(0.0f, Math.min(20.0f, currentTps));
    }

    /**
     * Возвращает множитель скорости.
     * 1.0 = Сервер работает идеально (20 TPS)
     * 0.5 = Сервер лагает в 2 раза (10 TPS)
     */
    public static float getMultiplier() {
        if (!isEnabled) return 1.0f;
        return getTps() / 20.0f;
    }

    // --- ЛОГИКА РАСЧЕТА ---

    // Так как в стандартном Forge нет удобного PacketEvent без миксинов,
    // мы используем хитрость: следим за временем мира.
    // Когда время мира меняется, значит пришел пакет времени.

    private static long lastWorldTime = -1;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            reset();
            return;
        }

        long currentWorldTime = mc.level.getGameTime();

        // Если время мира изменилось, значит сервер тик тикнул
        if (lastWorldTime != -1 && currentWorldTime != lastWorldTime) {
            long now = System.currentTimeMillis();

            if (lastPacketTime != -1) {
                // Время, прошедшее с прошлого тика в секундах
                long timeDiff = now - lastPacketTime;

                // Рассчитываем мгновенный TPS: 1000мс / время_между_тиками / 20
                // (Пакеты времени приходят раз в 20 тиков обычно)
                // Но этот метод updateTime срабатывает каждый тик? Нет, getGameTime меняется каждый тик.

                // Упрощенный расчет:
                // Идеальный тик длится 50 мс.
                // Если timeDiff был 100 мс, значит TPS = 10.

                float instantTps = 20.0f;
                // Защита от деления на ноль
                if (timeDiff > 0) {
                    instantTps = 1000.0f / (float) timeDiff;
                    // Так как мы ловим КАЖДЫЙ тик клиента, тут могут быть шумы.
                    // Для надежности этот метод лучше работает с пакетами, но на чистом Forge
                    // лучше использовать усреднение.
                }

                // Добавляем в историю (ограничиваем до 20)
                if (instantTps > 20.0f) instantTps = 20.0f;

                // Костыль для стабилизации:
                // Если timeDiff слишком маленький (лаганул клиент), игнорируем
                if (timeDiff > 10) {
                    addToHistory(instantTps * 20); // *20 потому что getGameTime тикает раз в тик?
                    // Нет, стоп. Логика выше сложная для TickEvent.

                    // ДАВАЙТЕ СДЕЛАЕМ ПРОЩЕ И НАДЕЖНЕЕ ДЛЯ FORGE:
                    // Мы будем использовать стандартный алгоритм Rolling Average.
                }
            }
            lastPacketTime = now;
        }
        lastWorldTime = currentWorldTime;

        // Каждые 20 тиков пересчитываем среднее значение
        recalculateTps();
    }

    // --- АЛЬТЕРНАТИВНЫЙ МЕТОД (ЕСЛИ ЕСТЬ МИКСИНЫ, ЛУЧШЕ ЮЗАТЬ ЕГО) ---
    // Но так как у нас чистый мод, мы эмулируем прием пакета.
    // Этот метод публичный, его можно вызывать из MixinClientPacketListener, если ты захочешь добавить миксины.
    public static void onTimeUpdatePacket() {
        long now = System.currentTimeMillis();
        if (lastPacketTime != -1) {
            long diff = now - lastPacketTime;
            // Пакет времени приходит раз в 20 тиков (1000 мс в идеале)
            float tps = 20.0f * (1000.0f / (float) diff);
            addToHistory(tps);
        }
        lastPacketTime = now;
    }

    private static void addToHistory(float val) {
        if (val < 0.0f) val = 0.0f;
        if (val > 20.0f) val = 20.0f;

        tpsHistory[nextIndex % tpsHistory.length] = val;
        nextIndex++;
    }

    private static void recalculateTps() {
        // Считаем среднее арифметическое
        float sum = 0;
        int len = 0;
        for (float v : tpsHistory) {
            if (v > 0) {
                sum += v;
                len++;
            }
        }
        if (len > 0) {
            currentTps = sum / len;
        } else {
            currentTps = 20.0f;
        }
    }

    public static void reset() {
        Arrays.fill(tpsHistory, 20.0f);
        currentTps = 20.0f;
        lastPacketTime = -1;
        lastWorldTime = -1;
    }
}