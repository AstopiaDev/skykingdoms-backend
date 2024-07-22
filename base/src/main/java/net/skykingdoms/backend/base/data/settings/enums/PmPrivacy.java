package net.skykingdoms.backend.base.data.settings.enums;

import lombok.NonNull;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 * TODO: validator
 */
public enum PmPrivacy {

    MAX{
        @Override
        public boolean test(final @NonNull AbstractGamer first, final @NonNull AbstractGamer second) {
            return true; //
        }
    },
    HIGH {
        @Override
        public boolean test(final @NonNull AbstractGamer first, final @NonNull AbstractGamer second) {
            // todo: if (second)
            return true;
        }
    },
    MEDIUM {
        @Override
        public boolean test(final @NonNull AbstractGamer first, final @NonNull AbstractGamer second) {
            return true;
        }
    },
    LOW {
        @Override
        public boolean test(final @NotNull AbstractGamer first, final @NotNull AbstractGamer second) {
            return first.isFriend(second.getGamerId()) /*|| second.isStaff()*//* || guild check */;
        }
    },
    NONE {
        @Override
        public boolean test(final @NotNull AbstractGamer first, final @NotNull AbstractGamer second) {
            return true;
        }
    };

    public static final PmPrivacy[] VALUES = values();

    public abstract boolean test(final @NotNull AbstractGamer first,
                              final @NotNull AbstractGamer second);

    public @NotNull String getNameLocalizationKey() {
        return "pmprivacy_" + name() + "_name";
    }

    public @NotNull String getLoreLocalizationKey() {
        return "pmprivacy_" + name() + "_lore";
    }

}
