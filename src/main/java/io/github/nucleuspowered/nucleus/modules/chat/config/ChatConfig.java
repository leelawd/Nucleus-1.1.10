/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chat.config;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.neutrino.annotations.Default;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Map;

@ConfigSerializable
public class ChatConfig {

    @Setting(value = "modify-chat", comment = "config.chat.modify")
    private boolean modifychat = true;

    @Setting(value = "use-group-templates", comment = "config.chat.useGroupTemplates")
    private boolean useGroupTemplates = true;

    @Setting(value = "templates")
    private TemplateConfig templates = new TemplateConfig();

    @Setting(value = "modify-main-message", comment = "config.chat.main")
    private boolean modifyMainMessage = true;

    @Setting(value = "overwrite-early-prefixes", comment = "config.chat.includeprefix")
    private boolean overwriteEarlyPrefixes = false;

    @Setting(value = "overwrite-early-suffixes", comment = "config.chat.includesuffix")
    private boolean overwriteEarlySuffixes = false;

    @Setting(value = "check-body-for-minecraft-prefix", comment = "config.chat.checkbody")
    private boolean checkBody = false;

    @Setting(value = "remove-link-underlines", comment = "config.chat.removeunderlines")
    private boolean removeBlueUnderline = true;

    @Setting(value = "me-prefix", comment = "config.chat.meprefix")
    @Default(value = "&7* {{displayName}} ", saveDefaultIfNull = true)
    private NucleusTextTemplateImpl mePrefix;

    public NucleusTextTemplateImpl getMePrefix() {
        return mePrefix;
    }

    public boolean isCheckBody() {
        return checkBody;
    }

    public boolean isModifychat() {
        return modifychat;
    }

    public boolean isUseGroupTemplates() {
        return useGroupTemplates;
    }

    public ChatTemplateConfig getDefaultTemplate() {
        return templates.getDefaultTemplate();
    }

    public Map<String, WeightedChatTemplateConfig> getGroupTemplates() {
        return ImmutableMap.copyOf(templates.getGroupTemplates());
    }

    public boolean isModifyMainMessage() {
        return modifyMainMessage;
    }

    public boolean isOverwriteEarlyPrefixes() {
        return overwriteEarlyPrefixes;
    }

    public boolean isOverwriteEarlySuffixes() {
        return overwriteEarlySuffixes;
    }

    public boolean isRemoveBlueUnderline() {
        return removeBlueUnderline;
    }
}
