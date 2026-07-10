package com.leaf.techjs.jei;

import com.leaf.techjs.CompatMods;
import com.leaf.techjs.TechSystemJS;

public class JEIRestarter {
    public static Runnable JEI_RESTART = () -> {};

    public static void restart() {
        if (!CompatMods.JEI.isLoaded()) return;
        if (JEI_RESTART != null) {
            TechSystemJS.LOGGER.info("TechSystemJS : JEI is restarting...");
            JEI_RESTART.run();
        } else {
            TechSystemJS.LOGGER.error("TechSystemJS : The JEI restarter was null");
        }
    }
}
