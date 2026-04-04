package me.coolaid.villagepillagetweaks.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.HashSet;
import java.util.Set;

public class HandsOffMyBlockSets {
    public static final Set<Block> WORKSTATIONS = new HashSet<>();

    static {
        WORKSTATIONS.add(Blocks.BARREL);
        WORKSTATIONS.add(Blocks.BELL);
        WORKSTATIONS.add(Blocks.BLAST_FURNACE);
        WORKSTATIONS.add(Blocks.BREWING_STAND);
        WORKSTATIONS.add(Blocks.CARTOGRAPHY_TABLE);
        WORKSTATIONS.add(Blocks.CAULDRON);
        WORKSTATIONS.add(Blocks.COMPOSTER);
        WORKSTATIONS.add(Blocks.FLETCHING_TABLE);
        WORKSTATIONS.add(Blocks.GRINDSTONE);
        WORKSTATIONS.add(Blocks.LECTERN);
        WORKSTATIONS.add(Blocks.LOOM);
        WORKSTATIONS.add(Blocks.SMITHING_TABLE);
        WORKSTATIONS.add(Blocks.SMOKER);
        WORKSTATIONS.add(Blocks.STONECUTTER);
    }
}