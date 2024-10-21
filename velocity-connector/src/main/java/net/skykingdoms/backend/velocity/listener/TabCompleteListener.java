package net.skykingdoms.backend.velocity.listener;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.CommandTabCompleteEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import lombok.NonNull;
import lombok.val;
import net.kyori.adventure.text.Component;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.packet.play.Tabcomplete;
import net.skykingdoms.backend.velocity.VelocityConnector;
import net.skykingdoms.backend.velocity.command.VelocityCommandCocker;
import net.skykingdoms.backend.velocity.connection.Connection;
import net.skykingdoms.backend.velocity.gamer.Gamer;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TabCompleteListener {
    private static final SuggestionProvider ASK_SERVER = new DummyProvider("minecraft:ask_server");

    @SuppressWarnings("unchecked")
    private CommandNode createDummyNode(final String command) {
        return LiteralArgumentBuilder.literal(command)
                .then(RequiredArgumentBuilder.<String, String>argument("args", StringArgumentType.greedyString())
                        .suggests(
                                ASK_SERVER
                        )).build();
    }

    @Subscribe
    @SuppressWarnings("unchecked")
    public void onDeclareCommands(final @NonNull PlayerAvailableCommandsEvent event) {
        val gamer = Gamer.getGamer(event.getPlayer());

/*        if (gamer != null) {
            for (val command : VelocityCommandCocker.INSTANCE.getCommands()) {
                event.getRootNode().addChild(createDummyNode(command));
            }
        }*/
    }

    @Subscribe
    public void onTabComplete(final @NonNull CommandTabCompleteEvent event, Continuation continuation) {
        Player player = event.getPlayer();
        Gamer gamer = Gamer.getGamer(player.getUniqueId());

        event.getSuggestions().clear();

        if (gamer == null) {
            continuation.resume();
            return;
        }

        String cursor = event.getPartialMessage();
        int commandSeparator = cursor.indexOf(' ');

        boolean isCommand = cursor.length() > 0 && cursor.charAt(0) == '/';

        if (isCommand) {
            if (commandSeparator == -1) {
                //event.getSuggestions().addAll(VelocityCommandCocker.INSTANCE.matchCommands(cursor, player.getCurrentServer().get().getServerInfo()));
                continuation.resume();
                return;
            }

            String baseCommand = cursor.substring(1, commandSeparator).toLowerCase();
            if (VelocityCommandCocker.INSTANCE.getCommands().contains(baseCommand)) {
                Connection.awaitPacket(new Tabcomplete.Request(gamer.getGamerId(), cursor))
                        .whenComplete((response, cause) -> {
                            Preconditions.checkNotNull(player.getCurrentServer(), "server");

                            if (player.getProtocolVersion().getProtocol() >= ProtocolVersion.MINECRAFT_1_13.getProtocol()) {
                                int start = cursor.lastIndexOf(' ') + 1;
                                int end = cursor.length();
                                StringRange range = StringRange.between(start, end);

                                List<Suggestion> brigadier = new LinkedList<>();
                                for (String s : response.getSuggestions()) {
                                    brigadier.add(new Suggestion(range, s));
                                }

                                List<String> suggestionStrings = new LinkedList<>();
                                for (Suggestion suggestion : brigadier) {
                                    suggestionStrings.add(suggestion.getText());
                                }

                                event.getSuggestions().addAll(suggestionStrings);
                            } else {
                                event.getSuggestions().addAll(response.getSuggestions());
                            }

                            continuation.resume();
                        });
                return;
                // cancel here or some shit event.setCancelled(true);
            }

            continuation.resume();
        }
    }

    private static final class DummyProvider implements SuggestionProvider<DummyProvider> {
        private final String key;

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<DummyProvider> context, SuggestionsBuilder builder) throws CommandSyntaxException {
            return builder.buildFuture();
        }

        public DummyProvider(String key) {
            this.key = key;
        }

        public String getKey() {
            return this.key;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof DummyProvider)) {
                return false;
            } else {
                DummyProvider other = (DummyProvider)o;
                Object this$key = this.getKey();
                Object other$key = other.getKey();
                if (this$key == null) {
                    if (other$key != null) {
                        return false;
                    }
                } else if (!this$key.equals(other$key)) {
                    return false;
                }

                return true;
            }
        }

        public int hashCode() {
            boolean PRIME = true;
            int result = 1;
            Object $key = this.getKey();
            result = result * 59 + ($key == null ? 43 : $key.hashCode());
            return result;
        }

        public String toString() {
            return "Commands.SuggestionRegistry.DummyProvider(key=" + this.getKey() + ")";
        }
    }
}
