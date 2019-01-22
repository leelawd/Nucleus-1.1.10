/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.argumentparsers.util.WrappedElement;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

@NonnullByDefault
public class NoModifiersArgument<T> extends WrappedElement {

    public static final String NO_COST_ARGUMENT = "nocost";
    public static final String NO_COOLDOWN_ARGUMENT = "nocooldown";
    public static final String NO_WARMUP_ARGUMENT = "nowarmup";

    public static final BiPredicate<CommandSource, Player> PLAYER_NOT_CALLER_PREDICATE =
        (c, o) -> !(c instanceof Player) || !((Player) c).getUniqueId().equals(o.getUniqueId());

    private final BiPredicate<CommandSource, T> test;
    private final List<String> argsToPut = Lists.newArrayList();

    public NoModifiersArgument(CommandElement element, @Nullable BiPredicate<CommandSource, T> test) {
        this(element, test, true, true, true);
    }

    @SuppressWarnings("SameParameterValue")
    protected NoModifiersArgument(CommandElement element, @Nullable BiPredicate<CommandSource, T> test, boolean isNoCost, boolean isNoWarmup, boolean isNoCooldown) {
        super(element);
        this.test = test == null ? (c, o) -> true : test;

        if (isNoCooldown) {
            argsToPut.add(NO_COOLDOWN_ARGUMENT);
        }

        if (isNoCost) {
            argsToPut.add(NO_COST_ARGUMENT);
        }

        if (isNoWarmup) {
            argsToPut.add(NO_WARMUP_ARGUMENT);
        }
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        getWrappedElement().parse(source, args, context);

        // We'll get here if there are no exceptions thrown.
        if (getKey() != null && context.hasAny(getKey()) && context.<T>getOne(getKey()).map(x -> test.test(source, x)).orElse(false)) {
            argsToPut.forEach(x -> context.putArg(x, true));
        }
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return getWrappedElement().complete(src, args, context);
    }
}
