package net.skykingdoms.backend.messagechannel.manager;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.enums.PmPrivacy;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class MessageManager implements Listener {

    public static final MessageManager INSTANCE = new MessageManager();

    Map<Gamer, Gamer> lastFrom = new ConcurrentHashMap<>(); //sender, receiver
    Map<Gamer, Gamer> lastTo = new ConcurrentHashMap<>();   //receiver, sender

    private void setLastRecipient(final @NonNull Gamer from,
                                  final @NonNull Gamer to) {
        lastFrom.put(from, to);
        lastTo.put(to, from);
    }

    public Gamer getLastFrom(final @NonNull Gamer gamer) {
        return lastFrom.get(gamer);
    }

    public Gamer getLastTo(final @NonNull Gamer gamer) {
        return lastTo.get(gamer);
    }

    public void unloadGamer(Gamer gamer) {
        lastFrom.remove(gamer);
        lastFrom.values().remove(gamer);

        lastTo.get(gamer);
        lastTo.values().remove(gamer);
    }

    private static final String MESSAGE = "{" +
            "\"text\":\"%s\"," +
            "\"clickEvent\":{" +
            "\"action\":\"suggest_command\"," +
            "\"value\":\"/msg %s \"" +
            "}," +
            "\"hoverEvent\":{" +
            "\"action\":\"show_text\"," +
            "\"value\":\"%s\"" +
            "}" +
            "}";

    private String getMessage(final @NonNull Language language,
                              final @NonNull String name,
                              final @NonNull String from,
                              final @NonNull String to,
                              final boolean sending,
                              final @NonNull String message) {
        return String.format(
                MESSAGE,

                getMessage(language, from, to, message), name,
                language.getMessage(false,
                        "message_" + (sending ? "send" : "receive") + "_hover",
                        "%name%", name
                )
        );
    }

    private String getMessage(final @NonNull Language language,
                              final @NonNull String from,
                              final @NonNull String to,
                              final @NonNull String message) {
        return language.getMessage(false, "message_format", "%from%", from, "%to%", to, "%message%",
                message.replace("\\", "\\\\"));
    }

    public void processMessage(final @NonNull Gamer from,
                               final @NonNull Gamer to,
                               final @NonNull String msg) {
        // todo: PM privacy settings
        switch ((PmPrivacy) to.getSetting(SettingType.PM_PRIVACY).getValue()) {
            case MAX:
                from.sendLocalizedMessage("message_blocked");
                return;
            case HIGH:
            case MEDIUM:
            case LOW:
                if (!to.isFriend(from.getGamerId())) {
                    from.sendLocalizedMessage("message_blocked");
                    return;
                }
                break;
            case NONE:
                break;
        }
        processReply(from, to, msg);
    }

    public void processReply(Gamer from, Gamer to, String msg) {
        setLastRecipient(from, to);

        if (msg.endsWith("[shuffled]")) {
            return;
        }

        from.sendRawMessage(getMessage((Language) from.getLanguage(),
                to.getName(), "Me", to.getPrefixedName(), true, msg));

        to.sendRawMessage(getMessage((Language) to.getLanguage(),
                from.getName(), from.getPrefixedName(), "Me", false, msg));

        for (Gamer gamer : Gamer.getGamers()) {
            if (gamer.getGamerId() == to.getGamerId() || gamer.getGamerId() == from.getGamerId()) {
                continue;
            }

            if (!gamer.isStaff()) {
                continue;
            }

            if (gamer.getPropertyOrDefault("social_spy_pm", 0) == 1) {
                gamer.sendMessage("§7[SocialSpy/PM] " + from.getName() + " §7-> " + to.getName() + " §7" + msg);
            }
        }
    }

}
