package me.coolaid.villagepillagetweaks.data;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/*
 * This new class migrates handsoffmyblock_marked.dat files from pre-26.1
 * world folder structure to the new 26.1 dimension directory layout.
 *
 * Old layout (pre-26.1):
 *   (overworld) world/data/handsoffmyblock_marked.dat
 *   (nether) world/DIM-1/data/handsoffmyblock_marked.dat
 *   (the end) world/DIM1/data/handsoffmyblock_marked.dat
 *
 * New layout (26.1):
 *   world/dimensions/minecraft/overworld/data/village-pillage-tweaks/handsoffmyblock_marked.dat
 *   world/dimensions/minecraft/the_nether/data/village-pillage-tweaks/handsoffmyblock_marked.dat
 *   world/dimensions/minecraft/the_end/data/village-pillage-tweaks/handsoffmyblock_marked.dat
 *
 * Migration only runs if:
 *   - The old .dat file exists at the pre-26.1 path
 *   - The new .dat file does NOT already exist (never overwrites)
 *
 * Call HandsOffMyWorldDataMigrator.migrate(server) during ServerStartingEvent,
 * before world data storage is accessed.
 */
public class HandsOffMyWorldDataMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger("VillagePillageTweaks");

    private static final String OLD_FILE_NAME = "handsoffmyblock_marked.dat";
    private static final String NEW_RELATIVE_PATH = "village-pillage-tweaks/handsoffmyblock_marked.dat";

    public static void migrate(MinecraftServer server) {
        Path worldDir = server.getWorldPath(LevelResource.ROOT);
        Map<Path, Path> migrations = new LinkedHashMap<>();

        migrations.put(
                worldDir.resolve("data"),
                worldDir.resolve("dimensions/minecraft/overworld/data")
        );
        migrations.put(
                worldDir.resolve("DIM-1/data"),
                worldDir.resolve("dimensions/minecraft/the_nether/data")
        );
        migrations.put(
                worldDir.resolve("DIM1/data"),
                worldDir.resolve("dimensions/minecraft/the_end/data")
        );

        for (Map.Entry<Path, Path> entry : migrations.entrySet()) {
            Path oldDataDir = entry.getKey();
            Path newDataDir = entry.getValue();

            Path oldFile = oldDataDir.resolve(OLD_FILE_NAME);
            Path newFile = newDataDir.resolve(NEW_RELATIVE_PATH);

            if (!Files.exists(oldFile)) {
                continue;
            }

            if (Files.exists(newFile)) {
                LOGGER.info("[VillagePillageTweaks] Skipping migration for '{}': new file already exists at '{}'",
                        oldFile, newFile);
                continue;
            }

            try {
                Files.createDirectories(newFile.getParent());
                Files.copy(oldFile, newFile, StandardCopyOption.COPY_ATTRIBUTES);
                LOGGER.info("[VillagePillageTweaks] Migrated '{}' -> '{}'", oldFile, newFile);
            } catch (IOException e) {
                LOGGER.error("[VillagePillageTweaks] Failed to migrate '{}' -> '{}': {}",
                        oldFile, newFile, e.getMessage(), e);
            }
        }
    }
}