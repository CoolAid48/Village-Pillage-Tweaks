package me.coolaid.villagepillagetweaks.fabric.client;

import me.coolaid.villagepillagetweaks.config.ConfigScreenFactory;
import me.coolaid.villagepillagetweaks.fabric.VillagePillageTweaksFabric;
import net.minecraft.client.gui.screens.Screen;

public final class ConfigScreenFabric {
    private ConfigScreenFabric() {
    }

    public static Screen create(Screen parent) {
        return ConfigScreenFactory.create(parent, VillagePillageTweaksFabric::reloadMarkerItemFromConfig);
    }
}