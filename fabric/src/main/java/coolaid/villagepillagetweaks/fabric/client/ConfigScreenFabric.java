package coolaid.villagepillagetweaks.fabric.client;

import coolaid.villagepillagetweaks.config.ConfigScreenFactory;
import coolaid.villagepillagetweaks.fabric.VillagePillageTweaksFabric;
import net.minecraft.client.gui.screens.Screen;

public final class ConfigScreenFabric {
    private ConfigScreenFabric() {
    }

    public static Screen create(Screen parent) {
        return ConfigScreenFactory.create(parent, VillagePillageTweaksFabric::reloadMarkerItemFromConfig);
    }
}