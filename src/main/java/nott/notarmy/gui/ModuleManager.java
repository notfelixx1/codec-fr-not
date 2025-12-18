package nott.notarmy.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.sounds.SoundEvents;
import nott.notarmy.Client.*;
import nott.notarmy.Player.*;
import nott.notarmy.Client.*;
import nott.notarmy.Combat.*;
import nott.notarmy.Render.*;
import nott.notarmy.Movement.*;
import nott.notarmy.TargetHudEditor;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModuleManager {

    // Статический список модулей
    public static final List<Module> modules = new ArrayList<>();
    private static boolean initialized = false;

    // Метод инициализации (вызывать один раз при запуске)
    public static void init() {
        if (initialized) return;

        // === COMBAT ===
        add(new Module("AutoAxe", Category.COMBAT, () -> CombatEvents.isAutoAxeEnabled, v -> CombatEvents.isAutoAxeEnabled = v));

        Module multiClick = new Module("MultiClicker", Category.COMBAT, () -> MultiClicker.isEnabled, v -> MultiClicker.isEnabled = v);
        multiClick.addNum(new NumberSetting("Min CPS", 1, 20, MultiClicker.minCps, v -> MultiClicker.minCps = v.intValue()));
        multiClick.addNum(new NumberSetting("Max CPS", 1, 20, MultiClicker.maxCps, v -> MultiClicker.maxCps = v.intValue()));
        add(multiClick);

        Module velocity = new Module("Velocity", Category.COMBAT, () -> Velocity.isEnabled, v -> Velocity.isEnabled = v);
        velocity.addNum(new NumberSetting("Horizontal", 0, 1, Velocity.horizontal, v -> Velocity.horizontal = Math.round(v * 100.0) / 100.0));
        velocity.addNum(new NumberSetting("Vertical", 0, 1, Velocity.vertical, v -> Velocity.vertical = Math.round(v * 100.0) / 100.0));
        velocity.addNum(new NumberSetting("Chance", 0, 100, Velocity.chance, v -> Velocity.chance = v));
        add(velocity);

        Module hitboxes = new Module("Hitboxes", Category.COMBAT, () -> Hitboxes.isEnabled, v -> Hitboxes.isEnabled = v);
        hitboxes.addNum(new NumberSetting("Expand", 0, 1, Hitboxes.expand, v -> Hitboxes.expand = Math.round(v * 100.0) / 100.0));
        add(hitboxes);

        Module trigger = new Module("TriggerBot", Category.COMBAT, () -> TriggerBot.isEnabled, v -> TriggerBot.isEnabled = v);
        trigger.addBool(new BooleanSetting("Crits Only", () -> TriggerBot.onlyCrits, v -> TriggerBot.onlyCrits = v));
        add(trigger);

        Module aim = new Module("AimAssist", Category.COMBAT, () -> AimAssist.isEnabled, v -> AimAssist.isEnabled = v);
        aim.addNum(new NumberSetting("Speed", 0.1, 1.0, AimAssist.speed, v -> AimAssist.speed = Math.round(v * 100.0) / 100.0));
        aim.addNum(new NumberSetting("FOV", 10, 180, AimAssist.fov, v -> AimAssist.fov = v));
        add(aim);

        // === RENDER ===
        Module thud = new Module("TargetHUD", Category.RENDER, () -> TargetHud.isEnabled, v -> TargetHud.isEnabled = v);
        thud.addNum(new NumberSetting("Edit Pos >", 0, 1, 0, v -> { if(v > 0.1) Minecraft.getInstance().setScreen(new TargetHudEditor()); }));
        add(thud);

        add(new Module("PlayerESP", Category.RENDER, () -> ClientEvents.isEspEnabled, v -> ClientEvents.isEspEnabled = v));
        add(new Module("ChestESP", Category.RENDER, () -> ChestEsp.isChestEspEnabled, v -> ChestEsp.isChestEspEnabled = v));
        add(new Module("FullBright", Category.RENDER, () -> UtilityMod.isFullBrightEnabled, v -> UtilityMod.isFullBrightEnabled = v));
        add(new Module("NoWeather", Category.RENDER, () -> NoWeather.isEnabled, v -> NoWeather.isEnabled = v));
        add(new Module("NoFog", Category.RENDER, () -> NoFog.isEnabled, v -> NoFog.isEnabled = v));
        add(new Module("ArmorStatus", Category.RENDER, () -> ArmorStatus.isEnabled, v -> ArmorStatus.isEnabled = v));
        add(new Module("InfoHUD", Category.RENDER, () -> InfoHud.isEnabled, v -> InfoHud.isEnabled = v));
        add(new Module("Tracers", Category.RENDER, () -> Tracers.isEnabled, v -> Tracers.isEnabled = v));
        add(new Module("NoParticles", Category.RENDER, () -> NoParticles.isEnabled, v -> NoParticles.isEnabled = v));
        add(new Module("InactiveFPS", Category.RENDER, () -> InactiveFPS.isEnabled, v -> InactiveFPS.isEnabled = v));
        add(new Module("HP Indicator", Category.RENDER, () -> HealthIndicator.isHpEnabled, (v) -> HealthIndicator.isHpEnabled = v));


        // === MOVEMENT ===
        Module noslow = new Module("NoSlow", Category.MOVEMENT, () -> NoSlow.isEnabled, v -> NoSlow.isEnabled = v);
        noslow.addBool(new BooleanSetting("Items", () -> NoSlow.items, v -> NoSlow.items = v));
        noslow.addBool(new BooleanSetting("Webs", () -> NoSlow.webs, v -> NoSlow.webs = v));
        add(noslow);

        add(new Module("AutoSprint", Category.MOVEMENT, () -> UtilityMod.isSprintEnabled, v -> UtilityMod.isSprintEnabled = v));
        add(new Module("Spider", Category.MOVEMENT, () -> Spider.isEnabled, v -> Spider.isEnabled = v));

        Module step = new Module("Step", Category.MOVEMENT, () -> Step.isEnabled, v -> { Step.isEnabled = v; if (!v) Step.toggle(); });
        step.addNum(new NumberSetting("Height", 1, 2.5, Step.height, v -> Step.height = Math.round(v * 10.0f) / 10.0f));
        add(step);

        add(new Module("AirJump", Category.MOVEMENT, () -> AirJump.isEnabled, v -> AirJump.isEnabled = v));
        add(new Module("AntiVoid", Category.MOVEMENT, () -> AntiVoid.isEnabled, v -> AntiVoid.isEnabled = v));
        add(new Module("AutoWalk", Category.MOVEMENT, () -> AutoWalk.isEnabled, v -> { AutoWalk.isEnabled = v; if (!v) AutoWalk.toggle(); }));
        add(new Module("AutoJump", Category.MOVEMENT, () -> AutoJump.isEnabled, v -> AutoJump.isEnabled = v));

        // === PLAYER ===
        add(new Module("FastPlace", Category.PLAYER, () -> UtilityMod.isFastPlaceEnabled, v -> UtilityMod.isFastPlaceEnabled = v));
        add(new Module("AutoTool", Category.PLAYER, () -> AutoTool.isEnabled, v -> AutoTool.isEnabled = v));
        add(new Module("MemoryFix", Category.PLAYER, () -> MemoryFix.isEnabled, v -> MemoryFix.isEnabled = v));
        add(new Module("AntiLevitation", Category.PLAYER, () -> AntiLevitation.isEnabled, v -> AntiLevitation.isEnabled = v));
        add(new Module("TpsSync", Category.PLAYER, () -> TpsSync.isEnabled, v -> TpsSync.isEnabled = v));

        Module mine = new Module("AutoMine", Category.PLAYER, () -> AutoMineEvent.isMining, v -> { if (v) AutoMineEvent.toggle(); else { AutoMineEvent.isMining = false; AutoMineEvent.toggle(); } });
        mine.addNum(new NumberSetting("Time", 1, 60, AutoMineEvent.maxDurationSeconds, v -> AutoMineEvent.maxDurationSeconds = v));
        add(mine);

        initialized = true;
    }

    private static void add(Module m) {
        modules.add(m);
    }

    // ==========================================
    // КЛАССЫ (Перенесены из ClickGuiScreen)
    // ==========================================

    public enum Category { COMBAT, RENDER, MOVEMENT, PLAYER }

    public static class Module {
        public String name; public Category category; public Supplier<Boolean> getter; public Consumer<Boolean> setter;
        public boolean expanded = false; public double renderHeight = 25;
        public List<NumberSetting> numSettings = new ArrayList<>();
        public List<BooleanSetting> boolSettings = new ArrayList<>();

        public Module(String n, Category c, Supplier<Boolean> g, Consumer<Boolean> s) { name=n; category=c; getter=g; setter=s; }
        public void addNum(NumberSetting s) { numSettings.add(s); }
        public void addBool(BooleanSetting s) { boolSettings.add(s); }
        public void toggle() { setter.accept(!getter.get()); }
        public void setEnabled(boolean v) { setter.accept(v); }
        public double getSettingsHeight() { return (numSettings.size()*22) + (boolSettings.size()*22) + 5; }

        public void render(GuiGraphics cx, double x, double y, double w, double h, int mx, int my, int accent) {
            boolean en = getter.get(); boolean hov = RenderUtils.isHovered(mx, my, x, y, w, 25);
            int border = en ? accent : (hov ? 0xFF666666 : 0xFF333333);
            int text = en ? accent : 0xFFAAAAAA;
            RenderUtils.drawRoundedRect(cx, x, y, w, h, 6, 0xFF0E0E0E);
            RenderUtils.drawRoundedOutline(cx, x, y, w, 25, 6, 1.5, border);
            cx.drawString(Minecraft.getInstance().font, name, (int)x+10, (int)y+8, text, false);
            if(!numSettings.isEmpty()||!boolSettings.isEmpty()) cx.drawString(Minecraft.getInstance().font, expanded?"-":"+", (int)(x+w-15), (int)y+8, 0xFF555555, false);

            if(h>30) {
                double sy = y+30;
                for(NumberSetting n : numSettings) { n.render(cx, x+10, sy, w-20, 18, mx, my, accent); sy+=22; }
                for(BooleanSetting b : boolSettings) { b.render(cx, x+10, sy, w-20, 18, mx, my, accent); sy+=22; }
            }
        }
        public boolean mouseClicked(double mx, double my, double x, double y, double w, double h, int b) {
            if(RenderUtils.isHovered(mx, my, x, y, w, 25)) {
                if(b==0) { toggle(); Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); return true; }
                if(b==1) { expanded=!expanded; return true; }
            }
            if(expanded && h>30) {
                double sy = y+30;
                for(NumberSetting n : numSettings) { if(n.mouseClicked(mx,my,x+10,sy,w-20,18,b)) return true; sy+=22; }
                for(BooleanSetting bb : boolSettings) { if(bb.mouseClicked(mx,my,x+10,sy,w-20,18,b)) return true; sy+=22; }
            }
            return false;
        }
        public void mouseDragged(double mx, double my, int b, double x, double y) {
            if(!expanded) return;
            double sy = y+30;
            for(NumberSetting n : numSettings) { n.onDrag(mx, x+10, 0); sy+=22; }
        }
        public void mouseReleased(double mx, double my, int b) { for(NumberSetting n : numSettings) n.mouseReleased(mx, my, b); }
    }

    public static class NumberSetting {
        public String name; public double min, max, current; public Consumer<Double> onChange; private boolean dragging=false; private double lastX, lastW;
        public NumberSetting(String n, double min, double max, double cur, Consumer<Double> c) { name=n; this.min=min; this.max=max; current=cur; onChange=c; }
        public void render(GuiGraphics cx, double x, double y, double w, double h, int mx, int my, int accent) {
            lastX=x; lastW=w; if(dragging) update(mx);
            cx.drawString(Minecraft.getInstance().font, name, (int)x, (int)y+5, 0xFFAAAAAA, false);
            double sliderW = w / 2; double sliderX = x + w - sliderW; double sliderY = y + h/2;
            RenderUtils.drawRect(cx, sliderX, sliderY-1, sliderW, 2, 0xFF303030);
            double pct = (current-min)/(max-min);
            if(pct>0) RenderUtils.drawRect(cx, sliderX, sliderY-1, sliderW*pct, 2, accent);
            RenderUtils.drawRect(cx, sliderX + (sliderW*pct) - 2, sliderY - 4, 4, 8, 0xFFFFFFFF);
            String v = (max>20) ? String.valueOf((int)current) : String.valueOf(current); if(name.contains(">")) v="";
            cx.drawString(Minecraft.getInstance().font, v, (int)(sliderX - 25), (int)y+5, 0xFFFFFFFF, false);
        }
        public boolean mouseClicked(double mx, double my, double x, double y, double w, double h, int b) {
            if(RenderUtils.isHovered(mx,my,x + w/2,y,w/2,h)&&b==0){dragging=true; return true;} return false;
        }
        public void onDrag(double mx, double x, double w) { if(dragging) update(mx); }
        private void update(double mx) {
            double sliderX = lastX + lastW/2; double sliderW = lastW/2;
            double d = Math.min(sliderW, Math.max(0, mx - sliderX));
            double v = min + (d/sliderW)*(max-min);
            if(max>20) v=Math.round(v); else v=Math.round(v*100.0)/100.0;
            current=v; onChange.accept(current);
        }
        public void mouseReleased(double mx, double my, int b) { dragging=false; }
    }

    public static class BooleanSetting {
        public String name; public Supplier<Boolean> getter; public Consumer<Boolean> onChange;
        public BooleanSetting(String n, Supplier<Boolean> g, Consumer<Boolean> c) { name=n; getter=g; onChange=c; }
        public void toggle() { onChange.accept(!getter.get()); }
        public void render(GuiGraphics cx, double x, double y, double w, double h, int mx, int my, int accent) {
            cx.drawString(Minecraft.getInstance().font, name, (int)x, (int)y+5, 0xFFAAAAAA, false);
            double boxSize = 12; double boxX = x + w - boxSize; double boxY = y + 4;
            RenderUtils.drawRoundedOutline(cx, boxX, boxY, boxSize, boxSize, 3, 1, 0xFF505050);
            if(getter.get()) RenderUtils.drawRoundedRect(cx, boxX+2, boxY+2, boxSize-4, boxSize-4, 2, accent);
        }
        public boolean mouseClicked(double mx, double my, double x, double y, double w, double h, int b) { if(RenderUtils.isHovered(mx,my,x,y,w,h)&&b==0) { toggle(); Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F)); return true; } return false; }
    }
}