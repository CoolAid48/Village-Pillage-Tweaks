package me.coolaid.villagepillagetweaks.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class HandsOffMyVillagerMemoryHelper {

    private static final double RANGE = 64.0D;

    public static void invalidateNearbyVillagers(ServerLevel level, BlockPos pos, boolean isBed) {
        AABB box = new AABB(pos).inflate(RANGE);
        boolean isBell = level.getBlockState(pos).is(Blocks.BELL);

        for (Villager villager : level.getEntitiesOfClass(Villager.class, box)) {
            clearClaimedMemories(villager, level, pos, isBed, isBell);
        }
    }

    public static void clearClaimedMemories(Villager villager, ServerLevel level, BlockPos pos, boolean isBed, boolean isBell) {
        boolean clearedAny = false;

        if (clearIfMatches(villager, level, pos, MemoryModuleType.JOB_SITE)) clearedAny = true;
        if (clearIfMatches(villager, level, pos, MemoryModuleType.POTENTIAL_JOB_SITE)) clearedAny = true;
        if (isBed && clearIfMatches(villager, level, pos, MemoryModuleType.HOME)) clearedAny = true;

        // Technically not a workstation, but still a POI villagers can claim.
        if (isBell && clearIfMatches(villager, level, pos, MemoryModuleType.MEETING_POINT)) clearedAny = true;

        if (clearedAny && !villager.isSleeping()) {
            villager.getNavigation().stop();
        }
    }


    public static void nudgeNearbyVillagersToReacquirePoi(ServerLevel level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(RANGE);

        for (Villager villager : level.getEntitiesOfClass(Villager.class, box)) {
            villager.getBrain().eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

            if (!villager.isSleeping()) {
                villager.getNavigation().stop();
            }
        }
    }

    private static boolean clearIfMatches(Villager villager, ServerLevel level, BlockPos targetPos, MemoryModuleType<GlobalPos> type) {
        return villager.getBrain().getMemory(type)
                .filter(mem -> mem.dimension().equals(level.dimension()) && mem.pos().equals(targetPos))
                .map(mem -> {
                    villager.getBrain().eraseMemory(type);

                    // Keep POI ticket counts in sync with erased memories so beds/workstations can be reclaimed.
                    var poiManager = level.getPoiManager();
                    if (poiManager.getType(targetPos).isPresent()) poiManager.release(targetPos);
                    return true;
                })
                .orElse(false);
    }
}