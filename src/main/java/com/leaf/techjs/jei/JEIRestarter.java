package com.leaf.techjs.jei;

public class JEIRestarter {
    public static Runnable JEI_RESTART;

    public static void restart() {
        if (JEI_RESTART != null) {
            JEI_RESTART.run();
        }
    }
}
