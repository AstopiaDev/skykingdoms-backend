package net.skykingdoms.backend.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.RankChangedEvent;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.discord.DiscordBot;
import net.skykingdoms.backend.discord.type.MinecraftGroup;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RankListener implements Listener {

    public RankListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onRankChange(RankChangedEvent event) {
        Gamer gamer = (Gamer) event.getGamer();
        UserRank userRank = (UserRank) event.getRank();

        if (gamer.getPropertyOrDefault("discord_id", (long) 0) == (long) 0) {
            return;
        }

        Group group = userRank.getGroup();

        MinecraftGroup mcGroup;

        try {
            mcGroup = MinecraftGroup.valueOf(group.name().toUpperCase());
        } catch (Exception e) {
            return;
        }

        long discordId = gamer.getPropertyOrDefault("discord_id", (long) 0);
        Guild guild = DiscordBot.INSTANCE.guild();
        MinecraftGroup finalMcGroup = mcGroup;
        guild.retrieveMemberById(discordId).queue(member -> {
            if (member == null) {
                Logging.IMP.info("Invalid member");
                return;
            }

            for (MinecraftGroup minecraftGroup : MinecraftGroup.VALUES) {
                if (minecraftGroup == MinecraftGroup.LINKED) {
                    continue;
                }
                Role role = guild.getRoleById(minecraftGroup.id());
                if (role == null) {
                    continue;
                }
                if (member.getRoles().contains(role)) {
                    guild.removeRoleFromMember(member, role).queue();
                }
            }

            Role role = guild.getRoleById(finalMcGroup.id());

            if (role == null) {
                return;
            }

            guild.addRoleToMember(member, role).queue();
        });
    }

    @EventHandler
    public void onJoin(GamerJoinEvent event) {
        Gamer gamer = (Gamer) event.getGamer();

        if (gamer.getPropertyOrDefault("discord_id", (long) 0) == (long) 0) {
            return;
        }

        long discordId = gamer.getPropertyOrDefault("discord_id", (long) 0);

        List<String> ranks = ((UserRanks) gamer.getRanks()).getAllRanks()
                .stream()
                .map(UserRank::getGroup)
                .map(Group::name)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        Guild guild = DiscordBot.INSTANCE.guild();
        AsyncExecution.INSTANCE.run(() -> {
            guild.retrieveMemberById(discordId).queue(member -> {
                if (member == null) {
                    Logging.IMP.info("Invalid member");
                    return;
                }

                List<Role> roles = new ArrayList<>();

                for (MinecraftGroup minecraftGroup : MinecraftGroup.VALUES) {
                    if (minecraftGroup == MinecraftGroup.LINKED) {
                        continue;
                    }
                    Role role = guild.getRoleById(minecraftGroup.id());
                    if (role == null) {
                        continue;
                    }
                    if (member.getRoles().contains(role)) {
                         guild.removeRoleFromMember(member, role).complete();

                    }
                }

                for (String rank : ranks) {
                    MinecraftGroup group = null;

                    for (MinecraftGroup minecraftGroup : MinecraftGroup.VALUES) {
                        if (minecraftGroup.name().equalsIgnoreCase(rank)) {
                            group = minecraftGroup;
                        }
                    }

                    if (group == null) {
                        continue;
                    }

                    Role role = guild.getRoleById(group.id());
                    roles.add(role);
                }

                for (Role role : roles) {
                    guild.addRoleToMember(member, role).complete();
                }
            });
        });

    }
}
