package me.djtheredstoner.monkeyblur.commands;

import gg.essential.api.EssentialAPI;
import gg.essential.api.commands.Command;
import gg.essential.api.commands.DefaultHandler;
import gg.essential.api.commands.DisplayName;
import gg.essential.api.commands.Options;
import me.djtheredstoner.monkeyblur.MonkeyBlur;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import javax.annotation.Nullable;
import java.util.Optional;

public class MonkeyBlurCommand extends Command {

    public MonkeyBlurCommand() {
        super("monkeyblur");
    }

    @DefaultHandler
    public void handle(@DisplayName("action") @Options({"dump", "reload"}) @Nullable String action) {
        if (action == null) {
            EssentialAPI.getGuiUtil().openScreen(MonkeyBlur.instance.config.gui());
        } else {
            switch (action) {
                case "dump":
                    MonkeyBlur.instance.outputFb(MonkeyBlur.instance.frameBuffer);
                    break;
                case "reload":
                    MonkeyBlur.instance.loadShaders();
                    break;
            }
        }
    }

}
