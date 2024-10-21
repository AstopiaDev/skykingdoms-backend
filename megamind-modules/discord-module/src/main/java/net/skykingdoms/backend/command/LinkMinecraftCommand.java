package net.skykingdoms.backend.command;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.discord.DiscordBot;
import net.skykingdoms.backend.discord.controller.PlayerController;
import net.skykingdoms.backend.discord.type.MinecraftGroup;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WithAlias(
        "verify"
)
@NamedCommand("link")
public class LinkMinecraftCommand {
    @Executor
    @WithCondition("minArgs: 1")
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        String code = session.getArgument(0);

        if (gamer.getPropertyOrDefault("discord_id", (long) 0) != (long) 0) {
            gamer.sendMessage("§cYou are already linked to a Minecraft account.");
            return;
        }

        if (PlayerController.INSTANCE.hasCode(code)) {
            gamer.sendMessage("§aYou have successfully linked your Minecraft account!");

            String discordId = PlayerController.INSTANCE.getDiscordId(code);
            Guild guild = DiscordBot.INSTANCE.guild();

            gamer.setProperty("discord_id", Long.parseLong(discordId));

            guild.retrieveMemberById(discordId).queue(member -> {
                if (member == null) {
                    Logging.IMP.info("Invalid member");
                    return;
                }

                User user = member.getUser();
                Role role = guild.getRoleById(MinecraftGroup.LINKED.id());

                if (role == null) {
                    return;
                }

                guild.addRoleToMember(member, role).queue((empty) -> {
                    user.openPrivateChannel().queue((channel) -> {
                        channel.sendMessage("Your account has been linked to your Minecraft account! `" + gamer.getPlayerUniqueId() + "`").queue((msg) -> {
                        }, (error) -> {
                            // do nothing
                        });
                    });
                });

                PlayerController.INSTANCE.removeCode(discordId);

                List<String> ranks = ((UserRanks) gamer.getRanks()).getAllRanks()
                        .stream()
                        .map(UserRank::getGroup)
                        .map(Group::name)
                        .map(String::toUpperCase)
                        .collect(Collectors.toList());

                List<Role> roles = new ArrayList<>();

                for (MinecraftGroup minecraftGroup : MinecraftGroup.VALUES) {
                    if (minecraftGroup == MinecraftGroup.LINKED) {
                        continue;
                    }

                    Role mcRole = guild.getRoleById(minecraftGroup.id());

                    if (mcRole == null) {
                        continue;
                    }

                    if (member.getRoles().contains(mcRole)) {
                        guild.removeRoleFromMember(member, mcRole).queue();
                    }
                }

                for (String rank : ranks) {
                    MinecraftGroup group = null;

                    for (MinecraftGroup minecraftGroup : MinecraftGroup.VALUES) {
                        Logging.IMP.info(rank);
                        if (minecraftGroup.name().equalsIgnoreCase(rank)) {
                            group = minecraftGroup;
                        }
                    }

                    if (group == null) {
                        continue;
                    }

                    Role mcRole = guild.getRoleById(group.id());
                    roles.add(mcRole);
                }

                for (Role mcRole : roles) {
                    guild.addRoleToMember(member, mcRole).queue();
                }
            });
        }
    }
}
