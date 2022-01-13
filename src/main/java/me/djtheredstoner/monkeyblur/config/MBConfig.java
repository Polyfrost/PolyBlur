package me.djtheredstoner.monkeyblur.config;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;

import java.io.File;

public class MBConfig extends Vigilant {

    @Property(
        type = PropertyType.SWITCH, name = "Enabled",
        description = "Enable MonkeyBlur",
        category = "General"
    )
    public boolean enabled = true;

    @Property(
        type = PropertyType.SLIDER, name = "Blur Strength",
        description = "Set the motion blur strength",
        category = "General",
        min = 1, max = 100
    )
    public int strength = 10;

    public MBConfig() {
        super(new File("config/monkeyblur.toml"));
        initialize();
    }

}
