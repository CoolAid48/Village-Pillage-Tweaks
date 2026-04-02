package coolaid.villagepillagetweaks;

import coolaid.villagepillagetweaks.config.HandsOffMyConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VillagePillageTweaks {
    public static final String MOD_ID = "villagepillagetweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        HandsOffMyConfigManager.load();

        LOGGER.info("Getting marked POI data!");
        LOGGER.info("Getting Raid Spawn Radius!");
        LOGGER.info("Initializing tweaks!");
    }
}
