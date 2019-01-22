/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class MessageTargetArgument extends CommandElement {

    private final MessageHandler messageHandler;

    public MessageTargetArgument(@Nullable Text key) {
        super(key);
        messageHandler = Nucleus.getNucleus().getInternalServiceManager().getService(MessageHandler.class).get();
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return messageHandler.getTarget(args.next().toLowerCase()).orElseThrow(() -> args.createError(Text.of("No bot exists with that name")));
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        List<String> m = Lists.newArrayList(messageHandler.getTargetNames().keySet());
        try {
            String a = args.peek().toLowerCase();
            return m.stream().filter(x -> x.startsWith(a)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return m;
        }
    }
}
