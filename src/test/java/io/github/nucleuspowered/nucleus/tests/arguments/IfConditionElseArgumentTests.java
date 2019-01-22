/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.arguments;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.argumentparsers.IfConditionElseArgument;
import io.github.nucleuspowered.nucleus.tests.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

public class IfConditionElseArgumentTests extends TestBase {

    @Test
    public void testTrueOutcomeReturnsTrueInArgument() throws Exception {
        Assert.assertTrue(process((c, s) -> true));
    }

    @Test
    public void testFalseOutcomeReturnsFalseInArgument() throws Exception {
        Assert.assertFalse(process((c, s) -> false));
    }

    private boolean process(BiPredicate<CommandSource, CommandContext> cond) throws Exception {
        // Get the element
        CommandSource source = Mockito.mock(CommandSource.class);
        CommandArgs args = new CommandArgs("", Lists.newArrayList());
        CommandContext context = new CommandContext();
        new IfConditionElseArgument(new TrueArgument(), new FalseArgument(), cond).parse(source, args, context);
        return context.<Boolean>getOne("key").orElseThrow(NullPointerException::new);
    }

    @NonnullByDefault
    public static class TrueArgument extends CommandElement {

        public TrueArgument() {
            super(Text.of("key"));
        }

        @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            return true;
        }

        @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return Lists.newArrayList();
        }
    }


    @NonnullByDefault
    public static class FalseArgument extends CommandElement {

        public FalseArgument() {
            super(Text.of("key"));
        }

        @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            return false;
        }

        @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            return Lists.newArrayList();
        }
    }
}
