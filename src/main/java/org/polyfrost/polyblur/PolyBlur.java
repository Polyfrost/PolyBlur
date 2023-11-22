package org.polyfrost.polyblur;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import org.polyfrost.polyblur.blurs.phosphor.PhosphorBlur;
import org.polyfrost.polyblur.commands.PolyBlurCommand;
import org.polyfrost.polyblur.config.PolyBlurConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(
        modid = "@ID@"
)
public class PolyBlur {
    @Mod.Instance
    public static PolyBlur instance;

    public final PolyBlurConfig config = new PolyBlurConfig();

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        CommandManager.INSTANCE.registerCommand(new PolyBlurCommand());
        EventManager.INSTANCE.register(new PhosphorBlur());
    }
}
