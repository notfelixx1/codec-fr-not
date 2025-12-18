package nott.notarmy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import nott.notarmy.gui.ModuleManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDirectory, "NotArmyConfigs");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        // Убедимся, что модули загружены
        ModuleManager.init();
    }

    public static List<String> listConfigs() {
        List<String> names = new ArrayList<>();
        if (CONFIG_DIR.exists() && CONFIG_DIR.isDirectory()) {
            File[] files = CONFIG_DIR.listFiles((dir, name) -> name.endsWith(".json"));
            if (files != null) {
                for (File f : files) names.add(f.getName().replace(".json", ""));
            }
        }
        return names;
    }

    public static void saveConfig(String name) {
        File file = new File(CONFIG_DIR, name + ".json");
        JsonObject json = new JsonObject();

        // Ссылка на ModuleManager.modules
        for (ModuleManager.Module mod : ModuleManager.modules) {
            JsonObject modJson = new JsonObject();
            modJson.addProperty("enabled", mod.getter.get());

            JsonObject settingsJson = new JsonObject();
            for (ModuleManager.NumberSetting num : mod.numSettings) {
                settingsJson.addProperty(num.name, num.current);
            }
            for (ModuleManager.BooleanSetting bool : mod.boolSettings) {
                settingsJson.addProperty(bool.name, bool.getter.get());
            }
            modJson.add("settings", settingsJson);

            json.add(mod.name, modJson);
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfig(String name) {
        File file = new File(CONFIG_DIR, name + ".json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            for (ModuleManager.Module mod : ModuleManager.modules) {
                if (json.has(mod.name)) {
                    JsonObject modJson = json.getAsJsonObject(mod.name);

                    if (modJson.has("enabled")) {
                        boolean state = modJson.get("enabled").getAsBoolean();
                        if (mod.getter.get() != state) mod.setEnabled(state);
                    }

                    if (modJson.has("settings")) {
                        JsonObject settingsJson = modJson.getAsJsonObject("settings");

                        for (ModuleManager.NumberSetting num : mod.numSettings) {
                            if (settingsJson.has(num.name)) {
                                double val = settingsJson.get(num.name).getAsDouble();
                                num.current = val;
                                num.onChange.accept(val);
                            }
                        }

                        for (ModuleManager.BooleanSetting bool : mod.boolSettings) {
                            if (settingsJson.has(bool.name)) {
                                boolean val = settingsJson.get(bool.name).getAsBoolean();
                                if (bool.getter.get() != val) bool.toggle();
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteConfig(String name) {
        File file = new File(CONFIG_DIR, name + ".json");
        if (file.exists()) file.delete();
    }
}