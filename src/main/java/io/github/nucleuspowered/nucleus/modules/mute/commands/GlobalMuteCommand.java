/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@RunAsync
@NoModifiers
@NonnullByDefault
@Permissions
@RegisterCommand("globalmute")
public class GlobalMuteCommand extends AbstractCommand<CommandSource> {

    private final String on = "turn on";
    private final MuteHandler muteHandler;

    @Inject
    public GlobalMuteCommand(MuteHandler muteHandler) {
        this.muteHandler = muteHandler;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.bool(Text.of(on)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean turnOn = args.<Boolean>getOne(on).orElse(!muteHandler.isGlobalMuteEnabled());

        muteHandler.setGlobalMuteEnabled(turnOn);
        String onOff = plugin.getMessageProvider().getMessageFromKey(turnOn ? "standard.enabled" : "standard.disabled").get();
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.globalmute.status", onOff));
        MessageChannel.TO_ALL.send(plugin.getMessageProvider().getTextMessageWithFormat("command.globalmute.broadcast." + (turnOn ? "enabled" : "disabled")));

        return CommandResult.success();
    }
}
