package cc.polyfrost.polyblur.commands;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import cc.polyfrost.polyblur.blurs.monkey.MonkeyBlur;
import cc.polyfrost.polyblur.PolyBlur;
import net.minecraft.client.Minecraft;

@Command("polyblur")
public class PolyBlurCommand {

    @Main
    public static void handle() {
        PolyBlur.instance.config.openGui();
    }

    @SubCommand("output")
    public static final class OutputCommand {
        @Main
        public static void handle() {
            //MonkeyBlur.instance.outputFb(MonkeyBlur.instance.frameBuffer.width, MonkeyBlur.instance.frameBuffer.height, MonkeyBlur.instance.frameBuffer.framebufferTexture);
            MonkeyBlur.instance.outputFb(Minecraft.getMinecraft().getFramebuffer().framebufferTextureWidth, Minecraft.getMinecraft().getFramebuffer().framebufferTextureHeight, Minecraft.getMinecraft().getFramebuffer().framebufferTexture);
        }
    }

}
