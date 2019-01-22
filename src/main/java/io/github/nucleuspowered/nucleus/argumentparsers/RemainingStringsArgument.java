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
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
public class RemainingStringsArgument extends CommandElement {

    public RemainingStringsArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        // ignored.
        args.nextIfPresent();
        return args.getRaw().substring(args.getRawPosition()).trim();
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        super.parse(source, args, context);
        while(args.hasNext()) {
            args.next();
        }
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String[] l = args.getRaw().substring(args.getRawPosition()).split(" ");
        String check = l[l.length - 1];
        return Sponge.getServer().getOnlinePlayers().stream()
                .map(User::getName)
                .filter(x -> x.toLowerCase().startsWith(check.toLowerCase())).collect(Collectors.toList());
    }
}
