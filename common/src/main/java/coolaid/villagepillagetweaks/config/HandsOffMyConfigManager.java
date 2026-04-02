package coolaid.villagepillagetweaks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HandsOffMyConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path CONFIG_PATH = Path.of("config", "handsoffmyblock.json");
    private static HandsOffMyConfig CONFIG;

    public static void setConfigPath(Path path) {
        CONFIG_PATH = path;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                CONFIG = GSON.fromJson(
                        Files.readString(CONFIG_PATH),
                        HandsOffMyConfig.class
                );
            } catch (Exception e) {
                System.err.println("Failed to load config, loading defaults instead");
                e.printStackTrace();
                CONFIG = new HandsOffMyConfig();
                save();
            }
        } else {
            CONFIG = new HandsOffMyConfig();
            save();
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(CONFIG));
        } catch (IOException e) {
            System.err.println("Failed to save config, big oops");
            e.printStackTrace();
        }
    }

    public static HandsOffMyConfig get() {
        if (CONFIG == null) {
            load(); // safety net to make sure the config gets loaded
        }
        return CONFIG;
    }
}
