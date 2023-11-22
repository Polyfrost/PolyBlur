package org.polyfrost.polyblur.optifine;

import ofconfig.Config;

public class OFConfig {

    private static boolean optifine = false;

    public static String OF_RELEASE;

    static {
        try {
            OF_RELEASE = Config.OF_RELEASE;
            optifine = true;
        } catch (NoClassDefFoundError ignored) {}
        OF_RELEASE = "NONE";
    }

    public static boolean isOptifine() {
        return optifine;
    }

    public static boolean isShaders() {
        return isOptifine() && Config.isShaders();
    }

}
