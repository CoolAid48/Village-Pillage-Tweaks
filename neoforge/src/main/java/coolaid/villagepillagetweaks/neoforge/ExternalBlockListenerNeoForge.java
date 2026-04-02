package coolaid.villagepillagetweaks.neoforge;

import coolaid.villagepillagetweaks.util.HandsOffMyBlockAccessManager;
import coolaid.villagepillagetweaks.util.HandsOffMyBlockAccessManager.DestroyReason;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class ExternalBlockListenerNeoForge {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ExternalBlockListenerNeoForge::onServerLevelTick);
    }

    private static void onServerLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (BlockPos pos : HandsOffMyBlockAccessManager.getMarked(level).toArray(new BlockPos[0])) {
            BlockState last = HandsOffMyBlockAccessManager.getLastState(level, pos);

            if (last == null) continue;
            BlockState current = level.getBlockState(pos);

            // if (!current.equals(last)) {
            if (current.getBlock() != last.getBlock()) {
                DestroyReason reason = HandsOffMyBlockAccessManager.consumeReason(level, pos);
                if (reason == DestroyReason.UNKNOWN) {
                    reason = HandsOffMyBlockAccessManager.inferReasonFromCurrentState(current);
                }
                HandsOffMyBlockAccessManager.unmarkExternallyDestroyed(level, pos, reason);
            }
        }
    }
}