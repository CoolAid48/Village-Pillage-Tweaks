package coolaid.villagepillagetweaks.neoforge;

import coolaid.villagepillagetweaks.VillagePillageTweaks;
import coolaid.villagepillagetweaks.config.HandsOffMyConfigManager;
import coolaid.villagepillagetweaks.util.HandsOffMyBlockAccessManager;
import coolaid.villagepillagetweaks.util.HandsOffMyBlockSets;
import coolaid.villagepillagetweaks.util.HandsOffMyPoiRefreshHelper;
import coolaid.villagepillagetweaks.util.HandsOffMyVillagerMemoryHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod(HandsOffMyBlockNeoForge.MOD_ID)
public final class HandsOffMyBlockNeoForge {
    public static final String MOD_ID = "handsoffmyblock";
    public static Item MARKER_ITEM = Items.STICK;

    public HandsOffMyBlockNeoForge(IEventBus modEventBus) {
        // Register server-side setup
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onBlockBreak);

        VillagePillageTweaks.init();
        reloadMarkerItemFromConfig();
        ExternalBlockListenerNeoForge.register();
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        var config = HandsOffMyConfigManager.get();

        // Config marker item
        ItemStack held = event.getItemStack();
        Item markerItem = BuiltInRegistries.ITEM.getOptional(config.markerItem).orElse(Items.STICK);
        if (!held.is(markerItem)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getLevel().getBlockState(pos);
        Block block = state.getBlock();
        boolean isBed = block instanceof BedBlock;

        // CONFIG TOGGLES
        if (!isBed && (!HandsOffMyBlockSets.WORKSTATIONS.contains(block) || !config.enableWorkstationMarking)) return;
        if (config.requireSneaking && !event.getEntity().isCrouching()) return;
        if (isBed && !config.enableBedMarking) return;

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos otherHalf = isBed ? pos.relative(BedBlock.getConnectedDirection(state)) : null;
        boolean alreadyBlocked = HandsOffMyBlockAccessManager.isBlocked(serverLevel, pos)
                || (isBed && HandsOffMyBlockAccessManager.isBlocked(serverLevel, otherHalf));

        if (alreadyBlocked) {
            unmarkBlockAndInvalidate(serverLevel, pos, isBed, otherHalf, state);
            sendActionBarToPlayer(event.getEntity(),
                    Component.translatable("message.actionbar.unmarked").append(block.getName()).withStyle(ChatFormatting.GREEN)
            );
        } else {
            spawnAngryVillagerParticles(serverLevel, pos, otherHalf);
            markBlockAndInvalidate(serverLevel, pos, isBed, otherHalf, state);
            sendActionBarToPlayer(event.getEntity(),
                    Component.translatable("message.actionbar.marked").append(block.getName()).withStyle(ChatFormatting.RED)
            );
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();
        boolean isBed = block instanceof BedBlock;

        // Only care about blocks that can be marked
        if (!isBed && !HandsOffMyBlockSets.WORKSTATIONS.contains(block)) return;

        // Check if this block is marked, then remove POI
        if (HandsOffMyBlockAccessManager.isBlocked(serverLevel, pos)) {
            HandsOffMyBlockAccessManager.notifyBrokenUnmark(serverLevel, pos, state, HandsOffMyBlockNeoForge::sendActionBarToPlayer);
            unmarkBlockAndInvalidate(serverLevel, pos, isBed, isBed ? pos.relative(BedBlock.getConnectedDirection(state)) : null, state);
        }
    }

    public static void sendActionBarToPlayer(net.minecraft.world.entity.player.Player player, Component message) {
        if (HandsOffMyConfigManager.get().actionBarMessages) {
            player.displayClientMessage(message, true);
        }
    }

    public static void reloadMarkerItemFromConfig() {
        MARKER_ITEM = BuiltInRegistries.ITEM.getOptional(HandsOffMyConfigManager.get().markerItem).orElse(Items.STICK);
    }

    private static void spawnAngryVillagerParticles(ServerLevel level, BlockPos pos, BlockPos otherHalf) {
        AABB searchArea = new AABB(pos).inflate(48.0);
        if (otherHalf != null) searchArea = searchArea.minmax(new AABB(otherHalf).inflate(48.0));

        for (Villager villager : level.getEntitiesOfClass(Villager.class, searchArea)) {
            if (villagerHasMemoryForBlock(villager, pos, level) || (otherHalf != null && villagerHasMemoryForBlock(villager, otherHalf, level))) {
                spawnAngryParticlesAboveHead(level, villager);
            }
        }
    }

    // Checks if villager has a job (workstation), a potential job (pathfinding), or a home (bed)
    private static boolean villagerHasMemoryForBlock(Villager villager, BlockPos pos, ServerLevel level) {
        return memoryMatches(villager, level, pos, MemoryModuleType.JOB_SITE)
                || memoryMatches(villager, level, pos, MemoryModuleType.POTENTIAL_JOB_SITE)
                || memoryMatches(villager, level, pos, MemoryModuleType.HOME)
                || memoryMatches(villager, level, pos, MemoryModuleType.MEETING_POINT);
    }

    private static boolean memoryMatches(Villager villager, ServerLevel level, BlockPos pos, MemoryModuleType<GlobalPos> type) {
        return villager.getBrain().getMemory(type).map(mem -> mem.dimension().equals(level.dimension()) && mem.pos().equals(pos)).orElse(false);
    }

    private static void spawnAngryParticlesAboveHead(ServerLevel level, Villager villager) {
        double x = villager.getX();
        double y = villager.getY() + villager.getEyeHeight();
        double z = villager.getZ();

        level.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 6, 0.3, 0.1, 0.3, 0.0);
    }

    private static void markBlockAndInvalidate(ServerLevel level, BlockPos pos, boolean isBed, BlockPos otherHalf, BlockState state) {
        if (isBed && otherHalf != null) {
            BlockPos headPos = state.getValue(BedBlock.PART) == BedPart.HEAD ? pos : otherHalf;
            var poiManager = level.getPoiManager();
            if (poiManager.getType(headPos).isPresent()) poiManager.release(headPos);
        } else if (!isBed) {
            var poiManager = level.getPoiManager();
            if (poiManager.getType(pos).isPresent()) poiManager.release(pos);
        }
        HandsOffMyBlockAccessManager.markBlock(level, pos);
        HandsOffMyVillagerMemoryHelper.invalidateNearbyVillagers(level, pos, isBed);
        if (isBed && otherHalf != null) {
            HandsOffMyBlockAccessManager.markBlock(level, otherHalf);
            HandsOffMyVillagerMemoryHelper.invalidateNearbyVillagers(level, otherHalf, isBed);
        }
    }

    private static void unmarkBlockAndInvalidate(ServerLevel level, BlockPos pos, boolean isBed, BlockPos otherHalf, BlockState state) {
        HandsOffMyBlockAccessManager.unmarkBlock(level, pos);

        if (isBed && otherHalf != null) {
            HandsOffMyBlockAccessManager.unmarkBlock(level, otherHalf);
            // Refresh both bed halves to mirror break/place behavior for old beds.
            HandsOffMyPoiRefreshHelper.refresh(level, pos, level.getBlockState(pos));
            HandsOffMyPoiRefreshHelper.refresh(level, otherHalf, level.getBlockState(otherHalf));
            HandsOffMyVillagerMemoryHelper.nudgeNearbyVillagersToReacquirePoi(level, pos);
            HandsOffMyVillagerMemoryHelper.nudgeNearbyVillagersToReacquirePoi(level, otherHalf);
        } else if (!isBed) {
            HandsOffMyPoiRefreshHelper.refresh(level, pos, level.getBlockState(pos));
            HandsOffMyVillagerMemoryHelper.nudgeNearbyVillagersToReacquirePoi(level, pos);
        }
    }
}