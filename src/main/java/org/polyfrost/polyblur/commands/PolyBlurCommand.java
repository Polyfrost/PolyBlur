package org.polyfrost.polyblur.commands;

import org.polyfrost.oneconfig.api.commands.v1.factories.annotated.Command;
import org.polyfrost.polyblur.PolyBlur;
import org.polyfrost.utils.v1.dsl.ScreensKt;

@Command("polyblur")
public class PolyBlurCommand {

    @Command
    public void main() {
        ScreensKt.openUI(PolyBlur.instance.config);
    }

}
