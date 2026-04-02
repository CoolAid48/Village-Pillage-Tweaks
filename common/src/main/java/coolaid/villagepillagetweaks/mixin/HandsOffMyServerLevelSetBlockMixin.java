package coolaid.villagepillagetweaks.mixin;

import coolaid.villagepillagetweaks.util.HandsOffMyBlockAccessManager;
import coolaid.villagepillagetweaks.util.HandsOffMyBlockAccessManager.DestroyReason;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class HandsOffMyServerLevelSetBlockMixin {

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", at = @At("HEAD"), require = 0)
    private void handsOffMyBlock_captureDestroyReason3(BlockPos pos, BlockState newState, int flags, CallbackInfoReturnable<Boolean> cir) {
        handsOffMyBlock_captureDestroyReason(pos, newState);
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), require = 0)
    private void handsOffMyBlock_captureDestroyReason4(BlockPos pos, BlockState newState, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
        handsOffMyBlock_captureDestroyReason(pos, newState);
    }

    private void handsOffMyBlock_captureDestroyReason(BlockPos pos, BlockState newState) {
        if (!((Object) this instanceof ServerLevel self)) return;
        if (!HandsOffMyBlockAccessManager.isBlocked(self, pos)) return;

        BlockState current = self.getBlockState(pos);
        if (current.getBlock() == newState.getBlock()) return;

        HandsOffMyBlockAccessManager.rememberReason(self, pos, inferReason(newState));
    }

    private static DestroyReason inferReason(BlockState newState) {
        if (newState.is(Blocks.MOVING_PISTON) || newState.is(Blocks.PISTON_HEAD)) {
            return DestroyReason.PISTON;
        }
        if (newState.is(BlockTags.FIRE)) {
            return DestroyReason.FIRE;
        }

        for (StackTraceElement frame : Thread.currentThread().getStackTrace()) {
            String className = frame.getClassName();

            if (className.contains("ServerExplosion")) return DestroyReason.EXPLOSION;
            if (className.contains("FireBlock") || className.contains("BaseFireBlock") || className.contains("LavaFluid")) {
                return DestroyReason.FIRE;
            }
            if (className.contains("Piston")) return DestroyReason.PISTON;
        }

        return DestroyReason.REPLACED;
    }
}