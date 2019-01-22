/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.staffchat.commands.StaffChatCommand;
import io.github.nucleuspowered.nucleus.modules.staffchat.config.StaffChatConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StaffChatMessageChannel implements NucleusChatChannel.StaffChat {

    private static StaffChatMessageChannel INSTANCE = null;

    private boolean formatting = false;

    public static StaffChatMessageChannel getInstance() {
        Preconditions.checkState(INSTANCE != null, "StaffChatMessageChannel#Instance");
        return INSTANCE;
    }

    private final NucleusPlugin plugin;
    private final TextParsingUtils textParsingUtils;
    private final String basePerm;
    private NucleusTextTemplateImpl template;
    private TextColor colour;

    StaffChatMessageChannel(NucleusPlugin plugin) {
        this.plugin = plugin;
        textParsingUtils = plugin.getTextParsingUtils();
        plugin.registerReloadable(this::onReload);
        this.onReload();
        this.basePerm = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(StaffChatCommand.class).getBase();
        INSTANCE = this;
    }

    @Override
    public void send(@Nullable Object sender, Text original, ChatType type) {
        CommandSource source;
        if (sender == null || !(sender instanceof CommandSource)) {
            if (sender instanceof String) {
                source = new NamedSource((String) sender);
            } else {
                source = Sponge.getServer().getConsole();
            }
        } else {
            source = (CommandSource) sender;
        }

        Text prefix = template.getForCommandSource(source);
        NucleusChatChannel.StaffChat.super.send(sender, Text.of(prefix, colour, original), type);
    }

    @Override
    @Nonnull
    public Collection<MessageReceiver> getMembers() {
        List<MessageReceiver> c = Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.hasPermission(basePerm)).collect(Collectors.toList());
        c.add(Sponge.getServer().getConsole());
        return c;
    }

    @Override public boolean formatMessages() {
        return this.formatting;
    }

    private void onReload() {
        plugin.getConfigAdapter(StaffChatModule.ID, StaffChatConfigAdapter.class)
                .ifPresent(x -> {
                    this.formatting = x.getNodeOrDefault().isIncludeStandardChatFormatting();
                    this.template = x.getNodeOrDefault().getMessageTemplate();
                    this.colour = x.getNodeOrDefault().getColour();
                });
    }

    @NonnullByDefault
    private class NamedSource implements ProxySource {

        private final String name;

        NamedSource(String name) {
            this.name = name;
        }

        @Override
        public CommandSource getOriginalSource() {
            return Sponge.getServer().getConsole();
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            return Optional.of(getOriginalSource());
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return getOriginalSource().getContainingCollection();
        }

        @Override
        public SubjectData getSubjectData() {
            return getOriginalSource().getSubjectData();
        }

        @Override
        public SubjectData getTransientSubjectData() {
            return getOriginalSource().getTransientSubjectData();
        }

        @Override
        public Tristate getPermissionValue(Set<Context> contexts, String permission) {
            return getOriginalSource().getPermissionValue(contexts, permission);
        }

        @Override
        public boolean isChildOf(Set<Context> contexts, Subject parent) {
            return getOriginalSource().isChildOf(contexts, parent);
        }

        @Override
        public List<Subject> getParents(Set<Context> contexts) {
            return getOriginalSource().getParents();
        }

        @Override
        public Optional<String> getOption(Set<Context> contexts, String key) {
            return getOriginalSource().getOption(contexts, key);
        }

        @Override
        public String getIdentifier() {
            return getName();
        }

        @Override
        public Set<Context> getActiveContexts() {
            return getOriginalSource().getActiveContexts();
        }

        @Override
        public void sendMessage(Text message) {
            getOriginalSource().sendMessage(message);
        }

        @Override
        public MessageChannel getMessageChannel() {
            return getOriginalSource().getMessageChannel();
        }

        @Override
        public void setMessageChannel(MessageChannel channel) {
            getOriginalSource().setMessageChannel(channel);
        }
    }

}
