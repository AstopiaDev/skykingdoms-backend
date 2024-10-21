package net.skykingdoms.backend.discord.command.impl;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.skykingdoms.backend.discord.command.Command;
import net.skykingdoms.backend.discord.command.CommandExecutionSession;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.UUID;

public class ProfileCommand extends Command {
    public ProfileCommand() {
        super(
                Commands.slash("profile", "Get a player's profile.")
                .addOption(OptionType.STRING, "player", "The player's in-game name.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
        );
    }

    @Override
    public void execute(CommandExecutionSession session) {
        String playerName = session.interaction().getOption("player").getAsString();
        try {
            UUID uuid = UUID.randomUUID();
            BufferedImage templateImage = ImageIO.read(new File("player_profile.png"));
            BufferedImage resizedImage = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            Font font = null;

            // Calculate center of the image
            int centerX = resizedImage.getWidth() / 2;
            int centerY = resizedImage.getHeight() / 2;

            // Draw the template image centered on the resized image
            g2d.drawImage(templateImage, centerX - templateImage.getWidth() / 2, centerY - templateImage.getHeight() / 2, null);

            BufferedImage avatarImage = ImageIO.read(new URL("https://crafatar.com/avatars/" + uuid.toString()));

            // Draw shadow for the avatar image
            drawShadow(g2d, (centerX - avatarImage.getWidth() / 2) - 25, ((centerY - avatarImage.getHeight() / 2) - 50) - 25, avatarImage.getWidth() + 50, avatarImage.getHeight() + 50);

            // Draw the avatar image
            g2d.drawImage(avatarImage, centerX - avatarImage.getWidth() / 2, (centerY - avatarImage.getHeight() / 2) - 50, null);

            // Draw shadow for the text
            drawShadow(g2d, (centerX - avatarImage.getWidth() / 2) - 25, ((centerY - avatarImage.getHeight() / 2) - 125) - 25, g2d.getFontMetrics().stringWidth(playerName), g2d.getFontMetrics().getHeight());

            // Draw the player name text
            g2d.setFont(font);
            g2d.setColor(Color.WHITE);
            g2d.drawString(playerName, centerX - avatarImage.getWidth() / 2, (centerY - avatarImage.getHeight() / 2) - 125);

            File output = new File("player_profile_output.png");
            ImageIO.write(resizedImage, "png", output);

            replyFile(session, output);
            g2d.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to draw shadow behind an image or text
    private void drawShadow(Graphics2D g2d, int x, int y, int width, int height) {
        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(x, y, width, height);
    }
}
