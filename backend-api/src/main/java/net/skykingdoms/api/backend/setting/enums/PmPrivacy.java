package net.skykingdoms.api.backend.setting.enums;

import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ApiGamer;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 * TODO: validator
 */
public enum PmPrivacy {

    MAX{
        @Override
        public boolean test(final @NonNull ApiGamer first, final @NonNull ApiGamer second) {
            return true; //
        }
    },
    HIGH {
        @Override
        public boolean test(final @NonNull ApiGamer first, final @NonNull ApiGamer second) {
            // todo: if (second)
            return true;
        }
    },
    MEDIUM {
        @Override
        public boolean test(final @NonNull ApiGamer first, final @NonNull ApiGamer second) {
            return true;
        }
    },
    LOW {
        @Override
        public boolean test(final @NotNull ApiGamer first, final @NotNull ApiGamer second) {
            return true;//first.isFriend(second.getGamerId()) /*|| second.isStaff()*//* || guild check */;
        }
    },
    NONE {
        @Override
        public boolean test(final @NotNull ApiGamer first, final @NotNull ApiGamer second) {
            return true;
        }
    };

    public static final PmPrivacy[] VALUES = values();

    public abstract boolean test(final @NotNull ApiGamer first,
                              final @NotNull ApiGamer second);

    public @NotNull String getNameLocalizationKey() {
        return "pmprivacy_" + name() + "_name";
    }

    public @NotNull String getLoreLocalizationKey() {
        return "pmprivacy_" + name() + "_lore";
    }

}
