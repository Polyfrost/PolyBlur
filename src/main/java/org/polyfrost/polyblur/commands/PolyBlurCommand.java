package org.polyfrost.polyblur.commands;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import org.polyfrost.polyblur.PolyBlur;

@Command("polyblur")
public class PolyBlurCommand {

    @Main
    public static void handle() {
        PolyBlur.instance.config.openGui();
    }
}
