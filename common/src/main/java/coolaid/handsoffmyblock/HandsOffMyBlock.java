package coolaid.handsoffmyblock;

import coolaid.handsoffmyblock.config.HandsOffMyConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandsOffMyBlock {
    public static final String MOD_ID = "village-pillage-tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        HandsOffMyConfigManager.load();

        LOGGER.info("Getting marked POI data!");
        LOGGER.info("Calculating Raid Radius!");
        LOGGER.info("Initializing tweaks...");
    }
}
