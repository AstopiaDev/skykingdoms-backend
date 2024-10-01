package net.skykingdoms.backend.base.command.execution;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.backend.command.CommandExecutor;
import net.skykingdoms.api.backend.command.cooldown.WrappedCooldown;
import net.skykingdoms.api.backend.gamer.IGamer;
import net.skykingdoms.backend.base.command.CommandParams;
import net.skykingdoms.backend.base.command.CommandWrapper;
import net.skykingdoms.backend.base.command.condition.WrappedCompiledCommandCondition;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcompleter;

import java.util.List;
import java.util.concurrent.ExecutorService;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class WrappedCommandExecutor {

    private final int methodHash;

    CommandWrapper wrapper;
    ExecutorService threadExecutor;
    List<WrappedCompiledCommandCondition> conditions;
    CommandExecutor<ExecutionSession> executor;
    Tabcompleter[] tabcompleters;

    boolean withoutHelp;

    WrappedCooldown cooldown;

    @NonFinal String localizationKey;

    public boolean hasTabcompleters() {
        return tabcompleters != null;
    }

    public WrappedCommandExecutor(final @NonNull CommandParams params) {
        this.methodHash = params.getMethodHash();
        this.wrapper = params.getWrapper();
        this.conditions = params.getConditions();
        this.threadExecutor = params.getThreadExecutor();
        this.withoutHelp = params.isWithoutHelp();
        this.cooldown = params.getCooldown();
        this.localizationKey = params.getLocalizationKey();
        this.tabcompleters = params.getTabcompleters();

        CommandExecutor<ExecutionSession> executor = params.getExecutor();

        if (cooldown.getValue() > 0) {
            String key = getPropertyKey();

            this.executor = session -> {
                if (session.isConsole()) {
                    executor.execute(session);

                    return;
                }

                IGamer gamer = session.getGamer();
                // TODO: Write cooldown handling. This code no longer works
                //       because we rewrote properties system :/
//                Cooldown gamerCooldown = gamer.getCooldown(key);
//
//                if (gamerCooldown.isActive()) {
//                    throw new CommandTranslationException(
//                            "command_unavailable_cooldown",
//                            "%time%", Duration.durationWordsSec(session.getLanguage(), gamerCooldown.getAvailableAt() - TimeUtil.getNow())
//                    );
//                }
//
//                gamer.setCooldown(key, cooldown.getValue(), TimeUnit.MILLISECONDS);

                executor.execute(session);
            };
        } else {
            this.executor = executor;
        }
    }

    protected String getPropertyKey() {
        String key = asKey();
        String serverName = wrapper.getCommandManager().getServerName();

        if (serverName != null && cooldown.isCurrentServerOnly()) {
            key = serverName + "/" + key;
        }

        return key;
    }

    protected String asKey() {
        return wrapper.getName();
    }

    protected String asLocalizationKey() {
        return "command_" + wrapper.getName();
    }

    /**
     * Возвращает ключ локализации в формате command_$команда$(_$подкоманда$)
     */
    public String getLocalizationKey() {
        if (localizationKey == null) {
            localizationKey = asLocalizationKey();
        }

        return localizationKey;
    }

    @Override
    public String toString() {
        return "/" + wrapper.getName();
    }

}
