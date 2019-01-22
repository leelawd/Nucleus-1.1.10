/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Resorts a tab completion for {@link GenericArguments#user(Text)} so that online players appear first.
 */
public class ResortUserArgumentParser extends CommandElement {

    private final CommandElement userArgument;

    public ResortUserArgumentParser(Text key) {
        super(key);
        this.userArgument = GenericArguments.user(key);
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        Collection<String> onlinePlayers = Sponge.getServer().getOnlinePlayers().stream().map(User::getName).collect(Collectors.toList());
        return this.userArgument.complete(src, args, context).parallelStream().sorted((first, second) -> {
            boolean firstBool = onlinePlayers.contains(first);
            boolean secondBool = onlinePlayers.contains(second);
            if (firstBool == secondBool) {
                return first.compareTo(second);
            }

            return firstBool ? -1 : 1;
        }).collect(Collectors.toList());
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        this.userArgument.parse(source, args, context);
    }
}
