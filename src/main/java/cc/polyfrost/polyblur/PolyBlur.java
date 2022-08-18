package cc.polyfrost.polyblur;

import cc.polyfrost.oneconfig.events.EventManager;
import cc.polyfrost.oneconfig.utils.commands.CommandManager;
import cc.polyfrost.polyblur.blurs.phosphor.PhosphorBlur;
import cc.polyfrost.polyblur.commands.PolyBlurCommand;
import cc.polyfrost.polyblur.config.PolyBlurConfig;
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
        CommandManager.INSTANCE.registerCommand(PolyBlurCommand.class);
        EventManager.INSTANCE.register(new PhosphorBlur());
    }
}
