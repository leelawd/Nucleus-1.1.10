/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.staffchat.StaffChatMessageChannel;
import io.github.nucleuspowered.nucleus.modules.staffchat.datamodules.StaffChatTransientModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@RegisterCommand({"staffchat", "sc", "a"})
@NonnullByDefault
public class StaffChatCommand extends AbstractCommand<CommandSource> {

    private final String message = "message";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(new RemainingStringsArgument(Text.of(message)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<String> toSend = args.getOne(message);
        if (toSend.isPresent()) {
            if (src instanceof Player) {
                Player pl = (Player) src;
                MessageChannel mc = pl.getMessageChannel();
                pl.setMessageChannel(StaffChatMessageChannel.getInstance());
                pl.simulateChat(TextParsingUtils.addUrls(toSend.get()), Cause.of(NamedCause.source(src)));
                pl.setMessageChannel(mc);
            } else {
                StaffChatMessageChannel.getInstance()
                        .send(src, TextParsingUtils.addUrls(toSend.get()), ChatTypes.CHAT);
            }

            return CommandResult.success();
        }

        if (!(src instanceof Player)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.staffchat.consoletoggle"));
        }

        Player player = (Player)src;

        StaffChatTransientModule s = plugin.getUserDataManager().get(player).map(y -> y.getTransient(StaffChatTransientModule.class))
                .orElseGet(StaffChatTransientModule::new);

        boolean result = !(src.getMessageChannel() instanceof StaffChatMessageChannel);
        if (result) {
            s.setPreviousMessageChannel(player.getMessageChannel());
            src.setMessageChannel(StaffChatMessageChannel.getInstance());
        } else {
            src.setMessageChannel(s.getPreviousMessageChannel().orElse(MessageChannel.TO_ALL));
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.staffchat." + (result ? "on" : "off")));
        return CommandResult.success();
    }
}
