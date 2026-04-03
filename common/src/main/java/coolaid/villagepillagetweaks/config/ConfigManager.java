package coolaid.villagepillagetweaks.config;

import coolaid.villagepillagetweaks.VillagePillageTweaks;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;

public final class ConfigManager {
    private static Path configPath = Path.of("config", "handsoffmyblock.json");
    private static ConfigClassHandler<HandsOffMyConfig> handler = createHandler(configPath);

    private ConfigManager() {
    }

    public static synchronized void setConfigPath(Path path) {
        configPath = path;
        handler = createHandler(configPath);
    }

    public static void load() {
        handler.load();
    }

    public static synchronized void save() {
        handler.save();
    }

    public static HandsOffMyConfig get() {
        return handler.instance();
    }

    public static HandsOffMyConfig defaults() {
        return handler.defaults();
    }

    private static ConfigClassHandler<HandsOffMyConfig> createHandler(Path path) {
        return ConfigClassHandler.createBuilder(HandsOffMyConfig.class)
                .id(Identifier.parse(VillagePillageTweaks.MOD_ID + ":config"))
                .serializer(config -> GsonConfigSerializerBuilder.create(config)
                        .setPath(path)
                        .setJson5(false)
                        .build())
                .build();
    }

    public static final class HandsOffMyConfig {
        @SerialEntry
        public Identifier markerItem = Identifier.parse("minecraft:stick");

        @SerialEntry
        public boolean enableWorkstationMarking = true;

        @SerialEntry
        public boolean enableBedMarking = true;

        @SerialEntry
        public boolean requireSneaking = true;

        @SerialEntry
        public boolean pathfindingTweaks = false;

        @SerialEntry
        public boolean actionBarMessages = true;

        @SerialEntry
        public int raidSpawnRadius = 96;
    }
}