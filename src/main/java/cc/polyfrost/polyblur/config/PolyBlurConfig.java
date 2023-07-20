package cc.polyfrost.polyblur.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Dropdown;
import cc.polyfrost.oneconfig.config.annotations.Info;
import cc.polyfrost.oneconfig.config.annotations.Slider;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.InfoType;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;
import cc.polyfrost.polyblur.blurs.phosphor.PhosphorBlur;

public class PolyBlurConfig extends Config {

    @Info(
            text = "Phosphor / Moulberry blur will ONLY work if either Fast Render is disabled or Force Disable Fast Render is enabled.",
            size = 2,
            type = InfoType.WARNING
    )
    private boolean agajsjg = false;

    @Switch(
            name = "Force Disable Fast Render"
    )
    public boolean forceDisableFastRender = true;

    @Dropdown(
            name = "Blur Mode",
            options = {
                    "Monkey Blur",
                    "Phosphor Blur",
                    "Moulberry Blur"
            }
    )
    public int blurMode = 1;

    @Slider(
        name = "Blur Strength",
        min = 1, max = 10,
        step = 1
    )
    public int strength = 3;

    public PolyBlurConfig() {
        super(new Mod("PolyBlur", ModType.PVP, "/polyblur_dark.svg"), "polyblur.json");
        initialize();
        addListener("strength", () -> {
            if (blurMode == 1 && enabled) {
                PhosphorBlur.reloadIntensity();
            }
        });
        addListener("blurMode", PhosphorBlur::reloadBlur);
    }
}
