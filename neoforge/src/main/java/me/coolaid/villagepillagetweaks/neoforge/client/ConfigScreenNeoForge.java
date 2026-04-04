package me.coolaid.villagepillagetweaks.neoforge.client;

import me.coolaid.villagepillagetweaks.config.ConfigScreenFactory;
import me.coolaid.villagepillagetweaks.neoforge.VillagePillageTweaksNeoForge;
import net.minecraft.client.gui.screens.Screen;

public final class ConfigScreenNeoForge {
    private ConfigScreenNeoForge() {
    }

    public static Screen create(Screen parent) {
        return ConfigScreenFactory.create(parent, VillagePillageTweaksNeoForge::reloadMarkerItemFromConfig);
    }
}