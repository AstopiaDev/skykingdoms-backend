package net.skykingdoms.backend.messagechannel;

import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.plugin.Plugin;
import net.skykingdoms.backend.messagechannel.command.AppendCommand;
import net.skykingdoms.backend.messagechannel.command.MessageCommand;
import net.skykingdoms.backend.messagechannel.command.ReplyCommand;
import net.skykingdoms.backend.messagechannel.listener.MessageChannelListener;

public class MessageChannelPlugin extends Plugin {
    @Override
    public void onEnable() {
        new MessageChannelListener();
        MegamindApp.INSTANCE.commandManager().addCommand(AppendCommand.class);
        MegamindApp.INSTANCE.commandManager().addCommand(MessageCommand.class);
        MegamindApp.INSTANCE.commandManager().addCommand(ReplyCommand.class);
    }

    @Override
    public void onDisable() {
        MegamindApp.INSTANCE.removeCommand("append");
        MegamindApp.INSTANCE.removeCommand("message");
        MegamindApp.INSTANCE.removeCommand("reply");
    }
}
