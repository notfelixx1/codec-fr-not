package nott.notarmy.gui;

import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

public class ParticleEngine {
    private final List<Particle> particles = new ArrayList<>();

    public ParticleEngine(int count) {
        for(int i=0; i<count; i++) particles.add(new Particle());
    }

    public void render(GuiGraphics context, int width, int height, int mouseX, int mouseY) {
        for(Particle p : particles) {
            p.update(width, height);

            // Параллакс эффект (двигаются от мышки)
            double dx = (mouseX - width/2.0) * 0.02;
            double dy = (mouseY - height/2.0) * 0.02;

            RenderUtils.drawCircle(context, p.x + dx, p.y + dy, p.size, 0x40FFFFFF);
        }
    }

    private static class Particle {
        double x = Math.random() * 1000;
        double y = Math.random() * 1000;
        double vx = (Math.random() - 0.5) * 0.5;
        double vy = (Math.random() - 0.5) * 0.5;
        double size = Math.random() * 2 + 1;

        void update(int w, int h) {
            x += vx;
            y += vy;
            // Телепорт при выходе за границы
            if(x < 0) x = w; if(x > w) x = 0;
            if(y < 0) y = h; if(y > h) y = 0;
        }
    }
}