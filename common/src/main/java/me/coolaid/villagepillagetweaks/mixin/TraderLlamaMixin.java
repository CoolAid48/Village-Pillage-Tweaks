package me.coolaid.villagepillagetweaks.mixin;

import me.coolaid.villagepillagetweaks.config.ConfigManager;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TraderLlama.class)
public abstract class TraderLlamaMixin {
    @Inject(method = "canDespawn", at = @At(value = "HEAD"), cancellable = true)
    public void canDespawn(CallbackInfoReturnable<Boolean> info) {
        TraderLlama self = ((TraderLlama) (Object) this);
        if (ConfigManager.get().namedTraderLlamas && self.hasCustomName()) {
            info.setReturnValue(false);
        }
    }
}