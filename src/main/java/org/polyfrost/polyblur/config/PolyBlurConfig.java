package org.polyfrost.polyblur.config;

import org.polyfrost.oneconfig.api.config.v1.Config;
import org.polyfrost.oneconfig.api.config.v1.annotations.Dropdown;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.polyfrost.polyblur.blurs.phosphor.PhosphorBlur;

public class PolyBlurConfig extends Config {

    // Temporary!
    public boolean enabled = true;

    // @Info(
    //        text = "Phosphor / Moulberry blur will ONLY work if either Fast Render is disabled or Force Disable Fast Render is enabled.",
    //        size = 2,
    //        type = InfoType.WARNING
    //)
    private Runnable info = () -> { };

    @Switch(
            title = "Force Disable Fast Render"
    )
    public boolean forceDisableFastRender = true;

    @Dropdown(
            title = "Blur Mode",
            options = {
                    "Monkey Blur",
                    "Phosphor Blur",
                    "Moulberry Blur"
            }
    )
    public int blurMode = 1;

    @Slider(
        title = "Blur Strength",
        min = 1, max = 10,
        step = 1
    )
    public int strength = 3;

    public PolyBlurConfig() {
        super("polyblur.json", "/polyblur_dark.svg", "PolyBlur", Category.COMBAT);
        addCallback("strength", () -> {
            if (blurMode == 1 && enabled) {
                PhosphorBlur.reloadIntensity();
            }
        });
        addCallback("blurMode", PhosphorBlur::reloadBlur);
    }
}
