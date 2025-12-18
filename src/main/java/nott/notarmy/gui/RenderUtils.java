package nott.notarmy.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.Color;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class RenderUtils {

    private static int roundedProgramId = -1;
    private static long startTime = System.currentTimeMillis();

    // =========================================================
    //               VERTEX SHADER (Вершинный)
    // =========================================================
    // Отвечает за координаты углов на экране
    private static final String VERTEX_SHADER = """
        #version 120
        
        void main() {
            gl_TexCoord[0] = gl_MultiTexCoord0;
            gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
        }
    """;

    // =========================================================
    //              FRAGMENT SHADER (Фрагментный)
    // =========================================================
    // Отвечает за цвет каждого пикселя, закругления и свечение
    private static final String ROUNDED_FRAGMENT_SHADER = """
        #version 120
        
        uniform vec2 u_Size;       // Размер прямоугольника
        uniform float u_Radius;    // Радиус закругления
        uniform vec4 u_Color1;     // Цвет (Верх-Лево)
        uniform vec4 u_Color2;     // Цвет (Верх-Право)
        uniform vec4 u_Color3;     // Цвет (Низ-Право)
        uniform vec4 u_Color4;     // Цвет (Низ-Лево)
        uniform float u_Softness;  // Мягкость краев (для теней)
        
        // Функция расчета расстояния до краев закругленного прямоугольника (SDF)
        float roundedBoxSDF(vec2 centerPos, vec2 size, float radius) {
            return length(max(abs(centerPos) - size + radius, 0.0)) - radius;
        }
        
        // Функция смешивания 4 цветов (Градиент)
        vec4 mix4(vec4 c1, vec4 c2, vec4 c3, vec4 c4, vec2 uv) {
            vec4 top = mix(c1, c2, uv.x);
            vec4 bottom = mix(c4, c3, uv.x);
            return mix(top, bottom, uv.y);
        }

        void main() {
            // Преобразуем координаты текстуры (0..1) в пиксели
            vec2 pos = gl_TexCoord[0].st * u_Size;
            vec2 center = u_Size * 0.5;
            
            // Вычисляем дистанцию от пикселя до края фигуры
            float dist = roundedBoxSDF(pos - center, center, u_Radius);
            
            // Сглаживание краев (Anti-Aliasing)
            // smoothstep делает переход прозрачности мягким
            float alpha = 1.0 - smoothstep(0.0, u_Softness, dist);
            
            // Вычисляем цвет в этой точке (градиент)
            vec4 color = mix4(u_Color1, u_Color2, u_Color3, u_Color4, gl_TexCoord[0].st);
            
            // Применяем прозрачность
            gl_FragColor = vec4(color.rgb, color.a * alpha);
        }
    """;

    // Инициализация шейдеров
    public static void init() {
        if (roundedProgramId != -1) return;

        try {
            int vShader = createShader(VERTEX_SHADER, GL_VERTEX_SHADER);
            int fShader = createShader(ROUNDED_FRAGMENT_SHADER, GL_FRAGMENT_SHADER);

            roundedProgramId = glCreateProgram();
            glAttachShader(roundedProgramId, vShader);
            glAttachShader(roundedProgramId, fShader);
            glLinkProgram(roundedProgramId);

            int status = glGetProgrami(roundedProgramId, GL_LINK_STATUS);
            if (status == 0) {
                System.err.println("Shader Link Error: " + glGetProgramInfoLog(roundedProgramId, 1024));
                return;
            }

            // Шейдеры скомпилированы, удаляем исходники из памяти GPU
            glDeleteShader(vShader);
            glDeleteShader(fShader);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int createShader(String src, int type) {
        int shader = glCreateShader(type);
        glShaderSource(shader, src);
        glCompileShader(shader);

        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == 0) {
            System.err.println("Shader Compile Error: " + glGetShaderInfoLog(shader, 1024));
            return -1;
        }
        return shader;
    }
// =========================================================
    //               RENDERING METHODS
    // =========================================================

    /**
     * Рисует прямоугольник с закруглением через Шейдер.
     * Это дает идеальное качество без лесенки.
     */
    public static void drawRoundedRect(GuiGraphics context, double x, double y, double w, double h, double r, int color) {
        // Вызываем универсальный метод, передавая один цвет во все углы
        // Softness 1.0f - стандартное сглаживание краев
        drawRoundedGradientRect(context, x, y, w, h, r, 1.0f, color, color, color, color);
    }

    /**
     * Рисует градиентный закругленный прямоугольник (4 цвета).
     */
    public static void drawRoundedGradientRect(GuiGraphics context, double x, double y, double w, double h, double r, float softness, int c1, int c2, int c3, int c4) {
        // Инициализируем шейдеры при первом вызове, если еще не готовы
        if (roundedProgramId == -1) init();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Активируем программу шейдера
        glUseProgram(roundedProgramId);

        // Передаем параметры (Uniforms) в видеокарту
        setupUniforms(w, h, r, softness, c1, c2, c3, c4);

        // Рисуем простой квадрат с текстурными координатами (0,0 -> 1,1).
        // Шейдер использует эти координаты, чтобы понять, где закруглять.
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        Matrix4f matrix = context.pose().last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, (float)x, (float)(y + h), 0).uv(0, 1).endVertex();
        buffer.vertex(matrix, (float)(x + w), (float)(y + h), 0).uv(1, 1).endVertex();
        buffer.vertex(matrix, (float)(x + w), (float)y, 0).uv(1, 0).endVertex();
        buffer.vertex(matrix, (float)x, (float)y, 0).uv(0, 0).endVertex();

        tesselator.end();

        // Выключаем шейдер, чтобы не сломать рендер Майнкрафта
        glUseProgram(0);
        RenderSystem.disableBlend();
    }

    // =========================================================
    //               GLOW & SHADOWS (Свечение)
    // =========================================================

    /**
     * Рисует свечение (Bloom).
     * Шейдер делает это, просто размывая края (Softness).
     * Это работает быстрее и выглядит лучше, чем рисование 10 слоев.
     */
    public static void drawGlow(GuiGraphics context, double x, double y, double w, double h, int radius, int color) {
        // Рисуем прямоугольник чуть больше, но с огромным сглаживанием
        // softness = radius (чем больше радиус, тем мягче тень)
        drawRoundedGradientRect(context, x - radius, y - radius, w + radius*2, h + radius*2, radius, (float)radius, color, color, color, color);
    }

    // =========================================================
    //               INTERNAL HELPERS (Связь с GPU)
    // =========================================================

    private static void setupUniforms(double w, double h, double r, float softness, int c1, int c2, int c3, int c4) {
        // Размер
        int uSize = glGetUniformLocation(roundedProgramId, "u_Size");
        glUniform2f(uSize, (float) w, (float) h);

        // Радиус
        int uRadius = glGetUniformLocation(roundedProgramId, "u_Radius");
        // Ограничиваем радиус, чтобы не сломать шейдер (не больше половины стороны)
        float maxR = (float) Math.min(w, h) / 2.0f;
        glUniform1f(uRadius, Math.min((float)r, maxR));

        // Мягкость краев (для тени/свечения)
        int uSoftness = glGetUniformLocation(roundedProgramId, "u_Softness");
        glUniform1f(uSoftness, softness);

        // Цвета (отправляем 4 цвета в шейдер)
        uploadColor(glGetUniformLocation(roundedProgramId, "u_Color1"), c1);
        uploadColor(glGetUniformLocation(roundedProgramId, "u_Color2"), c2);
        uploadColor(glGetUniformLocation(roundedProgramId, "u_Color3"), c3);
        uploadColor(glGetUniformLocation(roundedProgramId, "u_Color4"), c4);
    }

    private static void uploadColor(int uniformId, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        glUniform4f(uniformId, r, g, b, a);
    }

// --- ВСТАВЬ ЧАСТЬ 3 НИЖЕ ---
// =========================================================
    //               COMPATIBILITY (Совместимость)
    // =========================================================

    // Рисует обычный прямоугольник (радиус 0) через шейдер
    public static void drawRect(GuiGraphics context, double x, double y, double w, double h, int color) {
        drawRoundedRect(context, x, y, w, h, 0, color);
    }

    // Рисует круг (радиус = половине ширины)
    public static void drawCircle(GuiGraphics context, double x, double y, double r, int color) {
        // x, y - это центр. Нам нужно левый верхний угол для прямоугольника
        drawRoundedRect(context, x - r, y - r, r * 2, r * 2, r, color);
    }

    // Рисует красивую обводку (Wireframe)
    // Метод маски: Рисуем большой цветной квадрат, а сверху - маленький черный
    public static void drawRoundedOutline(GuiGraphics context, double x, double y, double w, double h, double r, double thickness, int color) {
        // Внешний (Цвет)
        drawRoundedRect(context, x - thickness, y - thickness, w + thickness * 2, h + thickness * 2, r, color);
        // Внутренний (Вырезаем центр цветом фона)
        // 0xFF0E0E0E - это цвет фона нашего ClickGUI. Если поменяешь фон - поменяй и тут.
        drawRoundedRect(context, x, y, w, h, r, 0xFF0E0E0E);
    }

    public static void drawOutline(GuiGraphics context, double x, double y, double w, double h, double r, int color) {
        drawRoundedOutline(context, x, y, w, h, r, 1.0, color);
    }

    public static void drawScrollbar(GuiGraphics context, double x, double y, double width, double height, int color) {
        drawRoundedRect(context, x, y, width, height, width / 2.0, color);
    }

    // Текст (стандартный майнкрафт, шейдеры тут не нужны)
    public static void drawCenteredString(GuiGraphics context, net.minecraft.client.gui.Font font, String text, double x, double y, int color) {
        context.drawString(font, text, (int)(x - font.width(text) / 2), (int)y, color, false);
    }

    // =========================================================
    //               MATH & UTILS
    // =========================================================

    public static boolean isHovered(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static double lerp(double a, double b, double f) {
        return a + f * (b - a);
    }

    public static class ColorUtil {
        public static int getRainbow(float seconds, float saturation, float brightness) {
            float hue = (System.currentTimeMillis() % (int)(seconds * 1000)) / (float)(seconds * 1000);
            return Color.getHSBColor(hue, saturation, brightness).getRGB();
        }

        // Метод для получения цвета из компонентов (для совместимости)
        public static int getColor(int r, int g, int b, int a) {
            return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        }
    }
}