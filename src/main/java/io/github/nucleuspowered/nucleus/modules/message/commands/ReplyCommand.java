/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.AlertOnAfkArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.util.NucleusProcessing;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RedirectModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoHelpSubcommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

/**
 * Replies to the last player who sent a message.
 */
@Permissions(mainOverride = "message", suggestedLevel = SuggestedLevel.USER)
@NoHelpSubcommand
@RedirectModifiers(value = "message")
@RegisterCommand({"reply", "r"})
@EssentialsEquivalent({"r", "reply"})
@NonnullByDefault
public class ReplyCommand extends AbstractCommand<CommandSource> {

    private final String message = "message";

    private final MessageHandler handler;

    @Inject
    public ReplyCommand(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(message)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean b = handler.replyMessage(src, args.<String>getOne(message).get());
        if (b) {
            handler.getLastMessageFrom(Util.getUUID(src)).ifPresent(x -> {
                    if (x instanceof Player) {
                        plugin.getInternalServiceManager().getService(AFKHandler.class).filter(y -> y.isAfk((Player)x)).ifPresent(k ->
                            NucleusProcessing.addToContextOnSuccess(args, () -> AlertOnAfkArgument.getAction(src, (Player)x))
                        );
                    }
            });

            return CommandResult.success();
        }

        return CommandResult.empty();
    }

}
