package cc.polyfrost.polyblur.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class PolyBlurConfig extends Config {

    @Slider(
        name = "Blur Strength",
        min = 1, max = 10,
        step = 1
    )
    public int strength = 3;

    public PolyBlurConfig() {
        super(new Mod("PolyBlur", ModType.PVP), "polyblur.json");
    }

}
