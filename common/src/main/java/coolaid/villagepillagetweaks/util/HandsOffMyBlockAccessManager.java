package coolaid.villagepillagetweaks.util;

import coolaid.villagepillagetweaks.data.HandsOffMyMarkedBlocksData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.BiConsumer;

public final class HandsOffMyBlockAccessManager {

    public enum DestroyReason {
        BROKEN, EXPLOSION, FIRE, PISTON, REPLACED, UNKNOWN
    }

    private static final int NOTIFY_RADIUS = 64;

    private static final Map<ServerLevel, Map<BlockPos, BlockState>> LAST_STATES = new WeakHashMap<>();
    private static final Map<ServerLevel, Map<BlockPos, DestroyReason>> LAST_REASONS = new WeakHashMap<>();

    // MARKING METHODS
    public static void markBlock(ServerLevel level, BlockPos pos) {
        HandsOffMyMarkedBlocksData.get(level).mark(pos);
        track(level, pos);
    }

    public static void unmarkBlock(ServerLevel level, BlockPos pos) {
        HandsOffMyMarkedBlocksData.get(level).unmark(pos);
        clear(level, pos);
    }

    public static boolean isBlocked(ServerLevel level, BlockPos pos) {
        return HandsOffMyMarkedBlocksData.get(level).isMarked(pos);
    }

    public static Set<BlockPos> getMarked(ServerLevel level) {
        return HandsOffMyMarkedBlocksData.get(level).getAllMarked();
    }

    // NOTIFICATION METHODS
    public static void unmarkExternallyDestroyed(ServerLevel level, BlockPos pos, DestroyReason reason) {
        BlockState old = getLastState(level, pos);
        BlockState current = level.getBlockState(pos);

        unmarkBlock(level, pos);
        if (old == null) return;

        DestroyReason resolvedReason = reason != DestroyReason.UNKNOWN
                ? reason
                : inferReasonFromCurrentState(current);

        notifyUnmark(
                level,
                pos,
                old,
                resolvedReason,
                (player, message) -> player.sendOverlayMessage(message),
                true
        );
    }

    public static void notifyBrokenUnmark(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            BiConsumer<Player, Component> messageSender
    ) {
        notifyUnmark(level, pos, state, DestroyReason.BROKEN, messageSender, true);
    }

    private static void notifyUnmark(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            DestroyReason reason,
            BiConsumer<Player, Component> messageSender,
            boolean wrapReasonInParentheses
    ) {
        Component msg = Component.translatable("message.actionbar.unmarked")
                .append(state.getBlock().getName())
                .withStyle(ChatFormatting.GREEN)
                .append(reasonComponent(reason, wrapReasonInParentheses));

        notifyNearbyPlayers(level, pos, messageSender, msg);
    }

    private static Component reasonComponent(DestroyReason destroyReason, boolean wrapInParentheses) {
        String key = switch (destroyReason) {
            case BROKEN -> "component.actionbar.broken";
            case EXPLOSION -> "component.actionbar.explosion";
            case FIRE -> "component.actionbar.fire";
            case PISTON -> "component.actionbar.piston";
            case REPLACED -> "component.actionbar.replaced";
            case UNKNOWN -> "component.actionbar.unknown";
        };

        Component reasonText = wrapInParentheses
                ? Component.translatable("(%s)", Component.translatable(key))
                : Component.translatable(key);

        return Component.literal(" ")
                .append(reasonText)
                .withStyle(ChatFormatting.YELLOW);
    }

    private static void notifyNearbyPlayers(
            ServerLevel level,
            BlockPos pos,
            BiConsumer<Player, Component> messageSender,
            Component message
    ) {
        for (ServerPlayer player : level.getPlayers(p -> p.blockPosition().closerThan(pos, NOTIFY_RADIUS))) {
            messageSender.accept(player, message);
        }
    }

    public static DestroyReason inferReasonFromCurrentState(BlockState currentState) {
        if (currentState.is(Blocks.FIRE) || currentState.is(Blocks.SOUL_FIRE)) {
            return DestroyReason.FIRE;}

        if (currentState.is(Blocks.MOVING_PISTON) || currentState.is(Blocks.PISTON_HEAD)) {
            return DestroyReason.PISTON;
        }

        if (currentState.isAir()) {
            return DestroyReason.BROKEN;
        }

        return DestroyReason.REPLACED;
    }

    // TRACKING METHODS
    public static void track(ServerLevel level, BlockPos pos) {
        LAST_STATES
                .computeIfAbsent(level, l -> new HashMap<>())
                .put(pos, level.getBlockState(pos));
    }

    public static BlockState getLastState(ServerLevel level, BlockPos pos) {
        return LAST_STATES.getOrDefault(level, Map.of()).get(pos);
    }


    public static void rememberReason(ServerLevel level, BlockPos pos, DestroyReason reason) {
        LAST_REASONS
                .computeIfAbsent(level, l -> new HashMap<>())
                .put(pos, reason);
    }

    public static DestroyReason consumeReason(ServerLevel level, BlockPos pos) {
        Map<BlockPos, DestroyReason> map = LAST_REASONS.get(level);

        if (map == null) return DestroyReason.UNKNOWN;
        DestroyReason reason = map.remove(pos);
        return reason != null ? reason : DestroyReason.UNKNOWN;
    }

    private static void clear(ServerLevel level, BlockPos pos) {
        Map<BlockPos, BlockState> states = LAST_STATES.get(level);
        if (states != null) states.remove(pos);

        Map<BlockPos, DestroyReason> reasons = LAST_REASONS.get(level);
        if (reasons != null) reasons.remove(pos);
    }
}