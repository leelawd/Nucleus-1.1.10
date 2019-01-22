/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.inject.Inject;

@Permissions(prefix = "mail", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand(value = {"send", "s"}, subcommandOf = MailCommand.class)
public class SendMailCommand extends AbstractCommand<CommandSource> {

    private final MailHandler handler;
    private final PermissionRegistry permissionRegistry;

    private final String player = "subject";
    private final String message = "message";

    @Inject
    public SendMailCommand(MailHandler handler, PermissionRegistry permissionRegistry) {
        this.handler = handler;
        this.permissionRegistry = permissionRegistry;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            SelectorWrapperArgument.nicknameSelector(Text.of(player), NicknameArgument.UnderlyingType.USER),
            GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = args.<User>getOne(player).orElseThrow(() -> new CommandException(plugin.getMessageProvider().getTextMessageWithFormat("args.user.none")));
        CommandPermissionHandler permissionHandler = permissionRegistry.getPermissionsForNucleusCommand(MailCommand.class);

        // Only send mails to players that can read them.
        if (!permissionHandler.testBase(pl)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.mail.send.error", pl.getName()));
            return CommandResult.empty();
        }

        // Send the message.
        String m = args.<String>getOne(message).orElseThrow(() -> new CommandException(plugin.getMessageProvider().getTextMessageWithFormat("args.message.none")));
        if (src instanceof User) {
            handler.sendMail((User) src, pl, m);
        } else {
            handler.sendMailFromConsole(pl, m);
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.mail.send.successful", pl.getName()));
        return CommandResult.success();
    }
}
