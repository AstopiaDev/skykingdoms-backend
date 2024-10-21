package net.skykingdoms.backend.discord;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.skykingdoms.backend.DiscordPlugin;
import net.skykingdoms.backend.discord.controller.CommandController;
import net.skykingdoms.backend.discord.controller.FontController;
import net.skykingdoms.backend.discord.controller.PlayerController;
import net.skykingdoms.backend.discord.listener.CommandListener;
import net.skykingdoms.backend.discord.listener.MessageListener;
import net.skykingdoms.backend.discord.listener.ReadyListener;

import java.util.Random;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
public enum DiscordBot {
    INSTANCE
    ;

    Random random;
    String guildId;
    @Setter Guild guild;
    @Setter boolean serverActive = false;

    public void init() {
        guildId = DiscordPlugin.INSTANCE.getPlugin().getConfig().getString("guild-id");

        try {
            start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws InterruptedException {
        String token = DiscordPlugin.INSTANCE.getPlugin().getConfig().getString("token");
        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new ReadyListener())
                .addEventListeners(new MessageListener())
                .build();

        jda.awaitReady();

        this.random = new Random();
        guild = jda.getGuildById(guildId);

        initControllers();

        jda.addEventListener(new CommandListener());
        System.out.println("Bot is ready!");
    }

    private void initControllers() {
        CommandController.INSTANCE.init();
        FontController.INSTANCE.init();
        PlayerController.INSTANCE.init();
    }
}
