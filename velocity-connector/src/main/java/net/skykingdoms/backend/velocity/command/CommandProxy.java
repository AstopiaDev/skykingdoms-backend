package net.skykingdoms.backend.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.command.ICommand;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.backend.velocity.gamer.Gamer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class CommandProxy implements SimpleCommand {

    ICommand origin;

    CommandProxy(final @NonNull ICommand origin) {
        this.origin = origin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        origin.execute(source instanceof Player
                ? Gamer.getGamer((Player) source)
                : new ProxiedConsoleCommandSender(source), args);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        CommandSource source = invocation.source();
        ICommandSender sender;

        if (source instanceof Player) {
            sender = Gamer.getGamer((Player) source);
        } else {
            sender = new ProxiedConsoleCommandSender(source);
        }

        return origin.tabcomplete(sender, invocation.arguments());
    }


    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.supplyAsync(() -> {
            CommandSource source = invocation.source();
            ICommandSender sender;

            if (source instanceof Player) {
                sender = Gamer.getGamer((Player) source);
            } else {
                sender = new ProxiedConsoleCommandSender(source);
            }

            return origin.tabcomplete(sender, invocation.arguments());
        });
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return SimpleCommand.super.hasPermission(invocation);
    }
}
