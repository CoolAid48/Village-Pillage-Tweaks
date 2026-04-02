package coolaid.villagepillagetweaks.config;

import net.minecraft.resources.Identifier;

public class HandsOffMyConfig {

    public Identifier markerItem = Identifier.parse("minecraft:stick");
    public boolean enableWorkstationMarking = true; // Toggle workstations to be marked
    public boolean enableBedMarking = true; // Toggle beds to be marked
    public boolean requireSneaking = true; // Toggle require sneaking to mark
    public boolean pathfindingTweaks = false; // Toggle villager pathfinding tweaks
    public boolean actionBarMessages = true; // Toggle mod action bar messages
}
