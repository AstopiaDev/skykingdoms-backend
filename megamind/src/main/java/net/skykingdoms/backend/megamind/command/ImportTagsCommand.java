package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.util.config.Configuration;
import net.skykingdoms.api.backend.util.config.ConfigurationProvider;
import net.skykingdoms.api.backend.util.config.YamlConfiguration;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.tags.SeasonObtained;
import net.skykingdoms.backend.base.data.tags.TagsSqlHandler;
import net.skykingdoms.backend.base.data.tags.YearObtained;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

import java.io.File;
import java.util.List;

@NamedCommand("export")
public class ImportTagsCommand {

    @Executor
    @WithCondition("minArgs: 1")
    @WithCondition("group: admin")
    public static void execute(CoreExecutionSession session) {
        session.sendMessage("Importing tags to the database...");
        String fileName = session.getArgument(0);
        session.sendMessage("File name: " + fileName);
        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        val configFile = new File(fileName);

        if (!configFile.exists()) {
            session.sendMessage("File not found!");
            return;
        }

        try {
            Configuration configuration = provider.load(configFile);
            Configuration section = configuration.getSection("items");
            for (String key : section.getKeys()) {
                if (key.split("-").length < 3) {
                    continue;
                }
                if (!key.split("-")[2].equalsIgnoreCase("locked")) {
                    continue;
                }

                String name = section.getString(
                        "tag-" + key.split("-")[1] + "-equipped.view_requirement.requirements.equipped.output"
                ).substring(5);
                SeasonObtained obtained = null;
                YearObtained yearObtained = null;
                String nameId = section.getString("tag-" + key.split("-")[1] + "-unequipped.view_requirement.requirements.permission.permission").split("\\.")[1];

                List<String> lore = section.getStringList(key + ".lore");
                for (String line : lore) {
                    if (line.equalsIgnoreCase("§7You can unlock this tag")) {
                        obtained = SeasonObtained.PURCHASABLE;
                        break;
                    } else if (line.equalsIgnoreCase("§7You can no longer unlock this tag")) {
                        obtained = SeasonObtained.IMPOSSIBLE;
                        break;
                    }

                    String newLineXd = ChatColor.stripColor(line);

                    if (newLineXd.contains("Easter § St. Patricks")) {
                        obtained = SeasonObtained.EASTER_AND_ST_PATRICKS;
                        yearObtained = YearObtained.fromString("Y" + line.split(" ")[4].toUpperCase());
                        break;
                    }

                    if (line.equalsIgnoreCase("&c5th Server Anniversary Event")) {
                        String newLine = ChatColor.stripColor(line);
                        obtained = SeasonObtained.SERVER_ANNIVERSARY;
                        yearObtained = YearObtained.fromString("Y" + newLine.split(" ")[0].toUpperCase());
                    }

                    if (line.contains("&c2019 Birthday")) {
                        String newLine = ChatColor.stripColor(line);
                        obtained = SeasonObtained.BIRTHDAY;
                        yearObtained = YearObtained.fromString("Y" + newLine.split(" ")[0].toUpperCase());
                    }

                    if (line.contains("&aSt. Patricks")) {
                        obtained = SeasonObtained.ST_PATRICKS;
                        yearObtained = YearObtained.fromString("Y" + line.split(" ")[2].toUpperCase());
                        break;
                    }

                    if (!line.contains("Preview") && line.contains("Crate") || !line.contains("Preview") && line.contains("Parkour")) {
                        String newLine = ChatColor.stripColor(line);
                        Logging.IMP.info(newLine.split(" ")[0].toUpperCase());
                        Logging.IMP.info("Y" + newLine.split(" ")[1].toUpperCase());
                        Logging.IMP.info(newLine);
                        obtained = SeasonObtained.fromString(newLine.split(" ")[0].toUpperCase());
                        yearObtained = YearObtained.fromString("Y" + newLine.split(" ")[1].toUpperCase());
                        break;
                    }
                }

                TagsSqlHandler.INSTANCE.addTag(
                        name,
                        obtained,
                        yearObtained,
                        nameId
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
