package me.coolaid.villagepillagetweaks.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.coolaid.villagepillagetweaks.VillagePillageTweaks;
import me.coolaid.villagepillagetweaks.util.HandsOffMyBlockSets;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/* NOTES ON DATA REGISTRATION BELOW
 * - This is my first attempt at persistent world data that gets automatically saved and loaded, using codecs
 * for serialization (SavedDataType Registration).
 * - Each individual world and dimension has separate data; there are 3 separate handsoffmyblock_marked.dat
 * files for world folder + both DIM folders.
 * - I also implemented a system that cleans up the data when POIs are destroyed while marked (automatically
 * unmarking them and clearing them from the .dat file).
 * - For post-26.1 migration safety, when this data is first accessed for dimensions, we attempt a one-time copy from old
 * legacy DIM folders into the newer dimensions/* layout if old files exist and new files are missing.
 */

public class HandsOffMyMarkedBlocksData extends SavedData {
    private final Set<BlockPos> marked = new HashSet<>();
    private static final Set<String> MIGRATION_CHECKED_DIMENSIONS = ConcurrentHashMap.newKeySet();
    private static final List<String> LEGACY_DATA_FILE_CANDIDATES = List.of(
            "handsoffmyblock_marked.dat",
            "handsoffmyblock_handsoffmyblock_marked.dat"
    );

    // Define a Codec for serialization/deserialization
    private static final Codec<HandsOffMyMarkedBlocksData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.LONG.listOf().fieldOf("positions").xmap(
                    longs -> {
                        Set<BlockPos> set = new HashSet<>();
                        for (long l : longs) {
                            set.add(BlockPos.of(l));
                        }
                        return set;
                    },
                    set -> {
                        Set<Long> longs = new HashSet<>();
                        for (BlockPos pos : set) {
                            longs.add(pos.asLong());
                        }
                        return new ArrayList<>(longs);
                    }
            ).forGetter(data -> data.marked)).apply(instance, set -> {
                HandsOffMyMarkedBlocksData data = new HandsOffMyMarkedBlocksData();
                data.marked.addAll(set);
                return data;
            })
    );

    public static final SavedDataType<HandsOffMyMarkedBlocksData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("handsoffmyblock", "handsoffmyblock_marked"),
            HandsOffMyMarkedBlocksData::new, CODEC, null
    );

    public HandsOffMyMarkedBlocksData() {
    }

    public static HandsOffMyMarkedBlocksData get(ServerLevel level) {
        migrateLegacyDataIfNeeded(level);
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static void migrateLegacyDataIfNeeded(ServerLevel level) {
        String dimensionKey = level.dimension().toString();
        if (!MIGRATION_CHECKED_DIMENSIONS.add(dimensionKey)) return;

        Path worldRoot = level.getServer().getWorldPath(LevelResource.ROOT);
        Path modernDataDir = getModernDataDir(worldRoot, level);
        Path legacyDataDir = getLegacyDataDir(worldRoot, level);

        if (modernDataDir == null || legacyDataDir == null || !Files.isDirectory(legacyDataDir)) return;

        try {
            Files.createDirectories(modernDataDir);
            boolean copiedAny = false;

            for (String fileName : LEGACY_DATA_FILE_CANDIDATES) {
                Path oldFile = legacyDataDir.resolve(fileName);
                Path newFile = modernDataDir.resolve(fileName);

                if (!Files.exists(oldFile) || Files.exists(newFile)) continue;

                Files.copy(oldFile, newFile, StandardCopyOption.COPY_ATTRIBUTES);
                copiedAny = true;
            }

            if (copiedAny) {
                VillagePillageTweaks.LOGGER.info(
                        "[{}] Migrated legacy marked-block data from {} to {}",
                        dimensionKey, legacyDataDir, modernDataDir
                );
            }
        } catch (IOException e) {
            VillagePillageTweaks.LOGGER.warn(
                    "[{}] Failed migrating legacy marked-block data from {} to {}",
                    dimensionKey, legacyDataDir, modernDataDir, e
            );
        }
    }

    private static Path getModernDataDir(Path worldRoot, ServerLevel level) {
        if (level.dimension() == Level.OVERWORLD) {
            return worldRoot.resolve("data");
        }

        if (level.dimension() == Level.NETHER) {
            return worldRoot.resolve("dimensions").resolve("minecraft").resolve("the_nether").resolve("data");
        }

        if (level.dimension() == Level.END) {
            return worldRoot.resolve("dimensions").resolve("minecraft").resolve("the_end").resolve("data");
        }

        return null;
    }

    private static Path getLegacyDataDir(Path worldRoot, ServerLevel level) {
        if (level.dimension() == Level.NETHER) return worldRoot.resolve("DIM-1").resolve("data");
        if (level.dimension() == Level.END) return worldRoot.resolve("DIM1").resolve("data");
        return null;
    }

    public boolean isMarked(BlockPos pos) {
        return marked.contains(pos);
    }

    public void mark(BlockPos pos) {
        if (marked.add(pos)) {
            setDirty();
        }
    }

    public void unmark(BlockPos pos) {
        if (marked.remove(pos)) {
            setDirty();
        }
    }

    // Optionally get all marked positions
    public Set<BlockPos> getAllMarked() {
        return new HashSet<>(marked);
    }

    public void cleanupInvalidPositions(ServerLevel level) {
        Set<BlockPos> toRemove = new HashSet<>();
        PoiManager poiManager = level.getPoiManager();

        for (BlockPos pos : marked) {
            if (!level.isLoaded(pos)) continue;

            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();

            if (state.isAir() || (!(block instanceof BedBlock) && !HandsOffMyBlockSets.WORKSTATIONS.contains(block))) {

                // Remove from Minecraft's POI system if it exists
                if (poiManager.getType(pos).isPresent()) {
                    poiManager.remove(pos);
                }

                // Mark for removal from data
                toRemove.add(pos);
            }
        }

        if (!toRemove.isEmpty()) {
            marked.removeAll(toRemove);
            setDirty();
        }
    }
}