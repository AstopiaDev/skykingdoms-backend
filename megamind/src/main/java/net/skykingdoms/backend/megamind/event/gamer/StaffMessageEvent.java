package net.skykingdoms.backend.megamind.event.gamer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.language.LanguageContext;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.api.plugin.Event;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Setter
public final class StaffMessageEvent extends Event implements Cancellable {

    final LanguageContext context;

    boolean cancelled;

}
