/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
public final class SimpleProviderChoicesArgument extends CommandElement {

    private final Supplier<Map<String, ?>> choiceSupplier;

    public static SimpleProviderChoicesArgument withSetSupplier(Text key, Supplier<Set<String>> choices) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(choices);
        return new SimpleProviderChoicesArgument(key, () -> choices.get().stream().collect(Collectors.toMap(k -> k, k -> k)));
    }

    public static SimpleProviderChoicesArgument withMapSupplier(Text key, Supplier<Map<String, ?>> choices) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(choices);
        return new SimpleProviderChoicesArgument(key, choices);
    }

    private SimpleProviderChoicesArgument(Text key, Supplier<Map<String, ?>> choices) {
        super(Preconditions.checkNotNull(key));
        choiceSupplier = choices;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @SuppressWarnings("ConstantConditions") @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        GenericArguments.choices(getKey(), choiceSupplier.get()).parse(source, args, context);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return new ArrayList<>(choiceSupplier.get().keySet());
    }
}
