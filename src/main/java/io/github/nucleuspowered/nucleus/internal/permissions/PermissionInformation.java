/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.permissions;

import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.text.Text;

public class PermissionInformation {

    public final Text description;
    public final String plainDescription;
    public final SuggestedLevel level;
    public final boolean isOre;
    public final boolean isNormal;

    public static PermissionInformation getWithTranslation(String key, SuggestedLevel level) {
        return getWithTranslation(key, level, true, true);
    }

    public static PermissionInformation getWithTranslation(String key, SuggestedLevel level, boolean isNormal, boolean isOre) {
        return new PermissionInformation(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(key), level, isNormal, isOre);
    }

    public PermissionInformation(String description, SuggestedLevel level) {
        this(Text.of(description), level, true, true);
    }

    public PermissionInformation(String description, SuggestedLevel level, boolean isNormal, boolean isOre) {
        this(Text.of(description), level, isNormal, isOre);
    }

    private PermissionInformation(Text description, SuggestedLevel level, boolean isNormal, boolean isOre) {
        this.description = description;
        this.plainDescription = description.toPlain();
        this.level = level;
        this.isNormal = isNormal;
        this.isOre = isOre;
    }
}
