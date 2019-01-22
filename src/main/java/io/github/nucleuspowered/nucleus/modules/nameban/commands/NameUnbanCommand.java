/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.RegexArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.nameban.handlers.NameBanHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@Permissions(prefix = "nameban", mainOverride = "unban")
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand({"nameunban", "namepardon"})
public class NameUnbanCommand extends AbstractCommand<CommandSource> {

    private final String nameKey = "name";
    private final NameBanHandler handler;

    @Inject
    public NameUnbanCommand(NameBanHandler handler) {
        this.handler = handler;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new RegexArgument(Text.of(nameKey), Util.usernameRegexPattern, "command.nameban.notvalid")
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String name = args.<String>getOne(nameKey).get().toLowerCase();

        if (handler.removeName(name, Cause.of(NamedCause.owner(src)))) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nameban.pardon.success", name));
            return CommandResult.success();
        }

        throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.nameban.pardon.failed", name));
    }
}
