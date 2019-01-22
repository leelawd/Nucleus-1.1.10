/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.RegexArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.nameban.config.NameBanConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nameban.handlers.NameBanHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.stream.Collectors;

import javax.inject.Inject;

@Permissions
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand("nameban")
public class NameBanCommand extends AbstractCommand<CommandSource> {

    private final String nameKey = "name";
    private final String reasonKey = "reason";

    private final NameBanConfigAdapter nameBanConfigAdapter;
    private final NameBanHandler handler;

    @Inject
    public NameBanCommand(NameBanConfigAdapter nameBanConfigAdapter, NameBanHandler handler) {
        this.nameBanConfigAdapter = nameBanConfigAdapter;
        this.handler = handler;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new RegexArgument(Text.of(nameKey), Util.usernameRegexPattern, "command.nameban.notvalid", ((commandSource, commandArgs, commandContext) -> {
                try {
                    String arg = commandArgs.peek().toLowerCase();
                    return Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.getName().toLowerCase().startsWith(arg))
                        .map(User::getName)
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    return Lists.newArrayList();
                }
            })),
            GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of(reasonKey)))
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String name = args.<String>getOne(nameKey).get().toLowerCase();
        String reason = args.<String>getOne(reasonKey).orElse(nameBanConfigAdapter.getNodeOrDefault().getDefaultReason());

        if (handler.addName(name, reason, Cause.of(NamedCause.owner(src)))) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nameban.success", name));
            return CommandResult.success();
        }

        throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.nameban.failed", name));
    }
}
