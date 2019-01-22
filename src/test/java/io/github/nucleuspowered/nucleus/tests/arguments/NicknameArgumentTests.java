/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.arguments;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.tests.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NicknameArgumentTests extends TestBase {

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseOnlyAnExactMatchIsReturned() throws ArgumentParseException {
        List<?> list = getParser().accept("test", mockSource(), new CommandArgs("", new ArrayList<>()));

        Assert.assertEquals(1, list.size());
        Assert.assertEquals("test", ((User)list.get(0)).getName());
    }

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseWithNoExactMatchReturnsBothIfTheyOtherwiseMatch() throws ArgumentParseException {
        List<?> list = getParser().accept("tes", mockSource(), new CommandArgs("", new ArrayList<>()));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseWithNoExactMatchReturnsOneReturnsIfOnlyOneMatches() throws ArgumentParseException {
        List<?> list = getParser().accept("testt", mockSource(), new CommandArgs("", new ArrayList<>()));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("testtest", ((User)list.get(0)).getName());
    }

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseWithNoExactMatchReturnsNoneReturnIfNoneMatch() throws ArgumentParseException {
        List<?> list = getParser().accept("blah", mockSource(), new CommandArgs("", new ArrayList<>()));
        Assert.assertTrue(list.isEmpty());
    }

    private NicknameArgument.UserParser getParser() {
        // Setup the mock UserStorageService
        UserStorageService mockUss = getMockUserStorageService();

        // We're testing the UserParser
        return new NicknameArgument.UserParser(false, () -> mockUss);
    }

    private UserStorageService getMockUserStorageService() {
        GameProfile gp1 = Mockito.mock(GameProfile.class);
        GameProfile gp2 = Mockito.mock(GameProfile.class);
        Mockito.when(gp1.getName()).thenReturn(Optional.of("test"));
        Mockito.when(gp2.getName()).thenReturn(Optional.of("testtest"));

        UserStorageService mockUss = Mockito.mock(UserStorageService.class);
        Mockito.when(mockUss.getAll()).thenReturn(Lists.newArrayList(gp1, gp2));

        User u1 = Mockito.mock(User.class);
        Mockito.when(u1.getName()).thenAnswer(g -> gp1.getName().get());
        Mockito.when(u1.getPlayer()).thenAnswer(g -> Optional.empty());
        User u2 = Mockito.mock(User.class);
        Mockito.when(u2.getName()).thenAnswer(g -> gp2.getName().get());
        Mockito.when(u2.getPlayer()).thenAnswer(g -> Optional.empty());

        Mockito.when(mockUss.get(gp1)).thenReturn(Optional.of(u1));
        Mockito.when(mockUss.get(gp2)).thenReturn(Optional.of(u2));
        return mockUss;
    }

    private CommandSource mockSource() {
        CommandSource mock = Mockito.mock(CommandSource.class);
        Mockito.when(mock.hasPermission(Mockito.any())).thenReturn(true);
        return mock;
    }
}
