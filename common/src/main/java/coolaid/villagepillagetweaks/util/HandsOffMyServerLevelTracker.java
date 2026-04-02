package coolaid.villagepillagetweaks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;

public class HandsOffMyServerLevelTracker {
    private static final Map<String, ServerLevel> dimensionMap = new HashMap<>();

    public static void registerLevel(ServerLevel level) {
        dimensionMap.put(level.dimension().toString(), level);
    }

    public static void unregisterLevel(ServerLevel level) {
        dimensionMap.remove(level.dimension().toString());
    }

    public static ServerLevel getLevelForPos(BlockPos pos) {
        return dimensionMap.values().stream()
                .filter(level -> level.dimension() == Level.OVERWORLD)
                .findFirst()
                .orElse(null);
    }
}