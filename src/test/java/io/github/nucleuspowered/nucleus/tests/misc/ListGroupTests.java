/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.misc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.playerinfo.commands.ListPlayerCommand;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("SameParameterValue")
public class ListGroupTests {

    @Test
    public void testNonWeightedGroups() {
        Subject subject1 = createSubject("subject1");
        Subject subject2 = createSubject("subject2", subject1);
        Subject subject3 = createSubject("subject3", subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject2, subject1, subject3);

        List<Subject> sorted = subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(f -> 0, x, y)).collect(Collectors.toList());

        Assert.assertEquals(subject3, sorted.get(0));
        Assert.assertEquals(subject2, sorted.get(1));
        Assert.assertEquals(subject1, sorted.get(2));
    }

    @Test
    public void testWeightedGroups() {
        Subject subject1 = createSubject("subject1");
        Subject subject2 = createSubject("subject2", subject1);
        Subject subject2a = createSubjectWithWeight("subject2a", -1, subject1);
        Subject subject3 = createSubject("subject3", subject1, subject2);
        Subject subject4 = createSubjectWithWeight("subject4", 1, subject1, subject2);
        Subject subject5 = createSubjectWithWeight("subject5", 2, subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject1, subject2, subject2a, subject3, subject4, subject5);

        List<Subject> sorted = subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y))
            .collect(Collectors.toList());

        Assert.assertEquals(subject5, sorted.get(0));
        Assert.assertEquals(subject4, sorted.get(1));
        Assert.assertEquals(subject3, sorted.get(2));
        Assert.assertEquals(subject2, sorted.get(3));
        Assert.assertEquals(subject1, sorted.get(4));
        Assert.assertEquals(subject2a, sorted.get(5));
    }

    @Test
    public void testTreeWeightedGroups() {
        Subject subject1 = createSubject("subject1");
        Subject subject2 = createSubjectWithWeight("subject2", 4, subject1);
        Subject subject2a = createSubjectWithWeight("subject2a", -1, subject1);
        Subject subject3 = createSubject("subject3", subject1, subject2);
        Subject subject4 = createSubjectWithWeight("subject4", 1, subject1, subject2);
        Subject subject5 = createSubjectWithWeight("subject5", 2, subject1, subject2);

        List<Subject> subjects = Lists.newArrayList(subject1, subject2, subject2a, subject3, subject4, subject5);

        List<Subject> sorted = printWeights(subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y))
            .collect(Collectors.toList()));

        List<Subject> expectedOrder = Lists.newArrayList(subject3, subject2, subject5, subject4, subject1, subject2a);

        for (int i = 0; i < sorted.size(); i++) {
            Assert.assertEquals(
                "Index " + i + " is wrong! (expected: " + expectedOrder.get(i).getIdentifier() + ", got: " + sorted.get(i).getIdentifier() + ")",
                expectedOrder.get(i), sorted.get(i));
        }
    }

    @Test
    public void tesMultipleWeightedTrees() {
        // Using example from Rasgnarok

        // Gym Leader has a weight of 6, and Moderator 9
        // I tried as Helper(5) and QuestBuilder(8)
        // and Helper showed instead of QB
        // They are not in the same inheritance tree either
        // Swapping Helper for GymLeader reproduced the issue
        // Helper parents DeputyMod which in turn parents Moderator
        // Gym Leader parents 8 other groups
        // and QB inherits from Builder
        // So Helpers that are also Leaders are showing as Leaders

        List<Subject> subjects = Lists.newArrayList();

        Subject gymLeader = createSubjectWithWeight("gymleader", 6);
        subjects.add(gymLeader);

        List<Subject> gymleaders = createSubjects(8, "gym", gymLeader);
        subjects.addAll(gymleaders);

        Subject builder = createSubject("builder");
        subjects.add(builder);
        Subject questBuilder = createSubjectWithWeight("QuestBuilder", 8, builder);
        subjects.add(questBuilder);

        Subject helper = createSubjectWithWeight("helper", 5);
        Subject dep = createSubjectWithWeight("depmod", 7, helper);
        Subject mod = createSubjectWithWeight("mod", 9, dep);
        subjects.add(helper);
        subjects.add(dep);
        subjects.add(mod);

        List<Subject> sorted = printWeights(subjects.stream().sorted((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y))
            .collect(Collectors.toList()));

        List<Integer> integers = sorted.stream().map(x -> Util.getIntOptionFromSubject(x, "nucleus.list.weight").orElse(0)).collect(Collectors.toList());
        for (int i = 1; i < integers.size(); i++) {
            Assert.assertTrue(integers.get(i-1) >= integers.get(i));
        }
    }

    // Make 2 groups, example one admin group and another ace
    //
    // Set the order on nucleus config for them to show admin first then ace
    // group-order=[ admin, ace ]
    //
    // Set weight of admin to some number higher than ace
    //
    // Reload or restart server then /list to see if it worked

    @Test
    @SuppressWarnings("all")
    public void testWeightsAreBeingAppliedCorrectly() {
        // Create two groups.
        Subject admin = createSubjectWithWeight("admin", 1);
        Subject ace = createSubjectWithWeight("ace", 0);

        // The player is in both groups. Also, order is important.
        List<Subject> parents = Lists.newArrayList(admin, ace);
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getParents()).thenReturn(parents);
        Mockito.when(player.getParents(Mockito.anySet())).thenReturn(parents);

        // Create our map.
        Map<Player, List<String>> map = Maps.newHashMap();
        map.put(player, Lists.newArrayList("admin", "ace"));

        // No aliases.
        Map<String, String> aliases = Maps.newHashMap();

        // Now, let's run it through our method.
        Map<String, List<Player>> result = ListPlayerCommand.linkPlayersToGroups(parents, aliases, map);
        Assert.assertEquals("There should only be one entry", 1, result.size());
        List<Player> players = result.get("admin");

        Assert.assertNotNull("Players is null", players);
        Assert.assertEquals("There should only be one player!", 1, players.size());

        Assert.assertTrue("map is not empty", map.isEmpty());
    }

    @Test
    @SuppressWarnings("all")
    public void testWeightsAreBeingAppliedCorrectlyWithReversedGroupsInList() {
        // Create two groups.
        Subject admin = createSubjectWithWeight("admin", 1);
        Subject ace = createSubjectWithWeight("ace", 0);

        // Order of groups to check for.
        List<Subject> order = Lists.newArrayList(admin, ace);

        // The player is in both groups.
        List<Subject> parents = Lists.newArrayList(ace, admin);
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getParents()).thenReturn(parents);
        Mockito.when(player.getParents(Mockito.anySet())).thenReturn(parents);

        // Create our map.
        Map<Player, List<String>> map = Maps.newHashMap();
        map.put(player, Lists.newArrayList("ace", "admin"));

        // No aliases.
        Map<String, String> aliases = Maps.newHashMap();

        // Now, let's run it through our method.
        Map<String, List<Player>> result = ListPlayerCommand.linkPlayersToGroups(order, aliases, map);
        Assert.assertEquals("There should only be one entry", 1, result.size());
        List<Player> players = result.get("admin");

        Assert.assertNotNull("Players is null", players);
        Assert.assertEquals("There should only be one player!", 1, players.size());

        Assert.assertTrue("map is not empty", map.isEmpty());
    }

    @Test
    public void testTwoGroupsWithWeights() {
        Subject admin = createSubjectWithWeight("admin", 1);
        Subject ace = createSubjectWithWeight("ace", 0);
        Assert.assertEquals(-1, ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, admin, ace));
    }

    @Test
    public void testTwoGroupsReturnsTheCorrectOrder() {
        Subject admin = createSubjectWithWeight("admin", 1);
        Subject ace = createSubjectWithWeight("ace", 0);
        List<Subject> ls = Lists.newArrayList(ace, admin);
        ls.sort((x, y) -> ListPlayerCommand.groupComparison(ListPlayerCommand.weightingFunction, x, y));
        Assert.assertEquals(admin, ls.get(0));
        Assert.assertEquals(ace, ls.get(1));
    }

    @Test
    @SuppressWarnings("all")
    public void testWeightsAreBeingAppliedCorrectlyWithAliases() {
        // Create two groups.
        Subject admin = createSubjectWithWeight("admin", 1);
        Subject ace = createSubjectWithWeight("ace", 0);

        // The player is in both groups. Also, order is important.
        List<Subject> parents = Lists.newArrayList(admin, ace);
        Player player = Mockito.mock(Player.class);
        Mockito.when(player.getParents()).thenReturn(parents);
        Mockito.when(player.getParents(Mockito.anySet())).thenReturn(parents);

        // Create our map.
        Map<Player, List<String>> map = Maps.newHashMap();
        map.put(player, Lists.newArrayList("admin", "ace"));

        // No aliases.
        Map<String, String> aliases = Maps.newHashMap();
        aliases.put("admin", "Admin");
        aliases.put("ace", "Ace");

        // Now, let's run it through our method.
        Map<String, List<Player>> result = ListPlayerCommand.linkPlayersToGroups(parents, aliases, map);
        Assert.assertEquals("There should only be one entry", 1, result.size());
        List<Player> players = result.get("Admin");

        Assert.assertNotNull("Players is null", players);
        Assert.assertEquals("There should only be one player!", 1, players.size());

        Assert.assertTrue("map is not empty", map.isEmpty());
    }

    @Test
    @SuppressWarnings("all")
    public void testPlayerNotInAnyGroupIsLeftOver() {
        // Create two groups.
        Subject admin = createSubjectWithWeight("admin", 1);
        Subject ace = createSubjectWithWeight("ace", 0);

        // The player is in both groups. Also, order is important.
        List<Subject> parents = Lists.newArrayList(admin, ace);
        Player player = Mockito.mock(Player.class);
        Player player2 = Mockito.mock(Player.class);
        Mockito.when(player.getParents()).thenReturn(parents);
        Mockito.when(player.getParents(Mockito.anySet())).thenReturn(parents);
        Mockito.when(player2.getParents()).thenReturn(Lists.newArrayList());
        Mockito.when(player2.getParents(Mockito.anySet())).thenReturn(Lists.newArrayList());

        // Create our map.
        Map<Player, List<String>> map = Maps.newHashMap();
        map.put(player, Lists.newArrayList("admin", "ace"));
        map.put(player2, Lists.newArrayList());

        // No aliases.
        Map<String, String> aliases = Maps.newHashMap();
        aliases.put("admin", "Admin");
        aliases.put("ace", "Ace");

        // Now, let's run it through our method.
        Map<String, List<Player>> result = ListPlayerCommand.linkPlayersToGroups(parents, aliases, map);
        Assert.assertEquals("There should only be one entry", 1, result.size());
        List<Player> players = result.get("Admin");

        Assert.assertNotNull("Players is null", players);
        Assert.assertEquals("There should only be one player!", 1, players.size());

        Assert.assertEquals("One player should have been left over", 1, map.keySet().size());
    }

    private static List<Subject> printWeights(List<Subject> subjects) {
        subjects.forEach(x -> System.out.println(x.getIdentifier() + " - " + Util.getIntOptionFromSubject(x, "nucleus.list.weight")));
        return subjects;
    }

    private static List<Subject> createSubjects(int number, String prefix, Subject... parents) {
        List<Subject> subjects = Lists.newArrayList();
        for (int j = 1; j <= number; j++) {
            subjects.add(createSubject(prefix + j, parents));
        }

        return subjects;
    }

    private static Subject createSubject(String name, Subject... parents) {
        Subject subject = Mockito.mock(Subject.class);
        List<Subject> ls = Arrays.asList(parents);
        Mockito.when(subject.getIdentifier()).thenReturn(name);
        Mockito.when(subject.getOption(Mockito.anySetOf(Context.class), Mockito.eq("nucleus.list.weight")))
            .then(x -> ls.stream().map(y -> y.getOption("nucleus.list.weight")).filter(Optional::isPresent).findFirst().orElse(Optional.empty()));
        Mockito.when(subject.getOption(Mockito.eq("nucleus.list.weight")))
            .then(x -> ls.stream().map(y -> y.getOption("nucleus.list.weight")).filter(Optional::isPresent).findFirst().orElse(Optional.empty()));
        Mockito.when(subject.getParents()).thenReturn(Arrays.asList(parents));
        Mockito.when(subject.getParents(Mockito.anySetOf(Context.class))).thenReturn(Arrays.asList(parents));
        return subject;
    }

    private static Subject createSubjectWithWeight(String name, int weight, Subject... parents) {
        Subject subject = Mockito.mock(Subject.class);
        Mockito.when(subject.getIdentifier()).thenReturn(name);
        Mockito.when(subject.getOption(Mockito.anySetOf(Context.class), Mockito.eq("nucleus.list.weight")))
            .thenReturn(Optional.of(String.valueOf(weight)));
        Mockito.when(subject.getOption(Mockito.eq("nucleus.list.weight"))).thenReturn(Optional.of(String.valueOf(weight)));
        Mockito.when(subject.getParents()).thenReturn(Arrays.asList(parents));
        Mockito.when(subject.getParents(Mockito.anySetOf(Context.class))).thenReturn(Arrays.asList(parents));
        return subject;
    }
}
