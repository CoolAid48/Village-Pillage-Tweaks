package coolaid.villagepillagetweaks.mixin;

import coolaid.villagepillagetweaks.config.HandsOffMyConfigManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class HandsOffMyVillagerMixin extends AbstractVillager {

    public HandsOffMyVillagerMixin(EntityType<? extends AbstractVillager> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void villagerInit(EntityType<? extends AbstractVillager> entityType, Level world, CallbackInfo ci) {
        if (HandsOffMyConfigManager.get().pathfindingTweaks) {
            this.setPathfindingMalus(PathType.TRAPDOOR, -1);
        }
    }
}