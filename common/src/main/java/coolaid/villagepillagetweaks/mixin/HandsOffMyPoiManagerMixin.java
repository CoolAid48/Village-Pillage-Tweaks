package coolaid.villagepillagetweaks.mixin;

import coolaid.villagepillagetweaks.util.HandsOffMyBlockAccessManager;
import coolaid.villagepillagetweaks.util.HandsOffMyServerLevelTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PoiManager.class)
public class HandsOffMyPoiManagerMixin {

    @Inject(method = "getType", at = @At("HEAD"), cancellable = true)
    private void handsOffMyBlock_blockPOI(BlockPos pos, CallbackInfoReturnable<Optional<PoiType>> cir) {
        ServerLevel overworld = HandsOffMyServerLevelTracker.getLevelForPos(pos);
        if (overworld != null && HandsOffMyBlockAccessManager.isBlocked(overworld, pos)) {
            cir.setReturnValue(Optional.empty());
        }
    }
}