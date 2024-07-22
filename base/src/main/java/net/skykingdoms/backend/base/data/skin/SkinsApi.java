package net.skykingdoms.backend.base.data.skin;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.base.data.skin.exception.SkinException;
import net.skykingdoms.backend.base.data.skin.mojang.MojangApi;
import net.skykingdoms.backend.base.data.skin.sql.SkinsSqlHandler;

@UtilityClass
public class SkinsApi {

    public final Skin DEFAULT_SKIN = Skin.STEVE;

    /**
     * Retrieve skin data from database or mojang service
     *
     * @param skinName          - skin name
     * @return                  - skin data
     * @throws SkinException    - if exception raised on data loading
     */
    public Skin getCachedSkin(final @NonNull String skinName) throws SkinException {
        // if skin is default - no need to go further
        if (skinName.equalsIgnoreCase(DEFAULT_SKIN.getName())) {
            return DEFAULT_SKIN;
        }

        var skin = SkinsSqlHandler.INSTANCE.getSkinByName(skinName);

        if (skin != null && !skin.isExpired()) {
            return skin;
        }

        skin = MojangApi.getSkinTextures(skinName);
        SkinsSqlHandler.INSTANCE.addSkin(skin);

        return skin;
    }

}
