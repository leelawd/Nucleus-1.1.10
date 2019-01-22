/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusCommandException;
import io.github.nucleuspowered.nucleus.tests.util.TestModule;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.Tristate;

@SuppressWarnings("ALL")
public class CommandBaseTests extends TestBase {

    /**
     * Tests that if a {@link Player} is provided, they can execute a command for players.
     *
     * @throws CommandException
     */
    @Test
    public void testThatPlayersCanExecutePlayerCommand() throws CommandException {
        PlayerCommand cmd = new PlayerCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        Player mock = getMockPlayer();
        CommandResult result = cmd.process(mock, "");

        Assert.assertTrue("There should have been one success!", result.getSuccessCount().orElse(0) == 1);
    }

    /**
     * Tests that if a {@link CommandSource} that is not a {@link Player} is provided, the command fails with
     * {@link CommandResult#empty()}.
     *
     * @throws CommandException
     */
    @Test(expected = NucleusCommandException.class)
    public void testThatCommandSourcesCannotExecutePlayerCommands() throws CommandException {
        PlayerCommand cmd = new PlayerCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        CommandSource mock = getMockCommandSource();
        CommandResult result = cmd.process(mock, "");
    }

    /**
     * Tests that if a {@link CommandSource} that is not a player is provided, they can execute a standard command.
     *
     * @throws CommandException
     */
    @Test
    public void testThatCommandSourcesCanExecuteStandardCommand() throws CommandException {
        BasicCommand cmd = new BasicCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        CommandSource mock = getMockCommandSource();
        CommandResult result = cmd.process(mock, "");
        Assert.assertTrue("There should have been one success!", result.getSuccessCount().orElse(0) == 1);
    }

    /**
     * Tests that if a {@link Player} is provided, they can execute a standard command.
     *
     * @throws CommandException
     */
    @Test
    public void testThatPlayerSourcesCanExecuteStandardCommand() throws CommandException {
        BasicCommand cmd = new BasicCommand();
        getInjector().injectMembers(cmd);
        cmd.postInit();
        Player mock = getMockPlayer();
        CommandResult result = cmd.process(mock, "");
        Assert.assertTrue("There should have been one success!", result.getSuccessCount().orElse(0) == 1);
    }

    private Player getMockPlayer() {
        Player pl = Mockito.mock(Player.class);
        Mockito.when(pl.hasPermission(Matchers.any())).thenReturn(true);
        Mockito.when(pl.hasPermission(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(pl.getPermissionValue(Matchers.any(), Matchers.any())).thenReturn(Tristate.TRUE);
        return pl;
    }

    private CommandSource getMockCommandSource() {
        CommandSource pl = Mockito.mock(CommandSource.class);
        Mockito.when(pl.hasPermission(Matchers.any())).thenReturn(true);
        Mockito.when(pl.hasPermission(Matchers.any(), Matchers.any())).thenReturn(true);
        Mockito.when(pl.getPermissionValue(Matchers.any(), Matchers.any())).thenReturn(Tristate.TRUE);
        return pl;
    }

    private Injector getInjector() {
        return Guice.createInjector(new TestModule());
    }

    @Permissions
    @RegisterCommand("test")
    private class PlayerCommand extends AbstractCommand<Player> {

        @Override
        public String[] getAliases() {
            return new String[] { "test" };
        }

        @Override
        public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
            return CommandResult.success();
        }
    }

    @Permissions
    @RegisterCommand("test")
    private class BasicCommand extends AbstractCommand<CommandSource> {

        @Override
        public String[] getAliases() {
            return new String[] { "test" };
        }

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return CommandResult.success();
        }
    }

}
