/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.UniqueUserCountTransientModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.source.RemoteSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;

public final class Tokens implements NucleusMessageTokenService.TokenParser {

    private final Map<String, Translator> translatorMap = Maps.newHashMap();

    Tokens() {
        translatorMap.put("name", (p, v, m) -> Optional.of(Nucleus.getNucleus().getTextParsingUtils().addCommandToName(getFromVariableIfExists(p, v, m))));
        translatorMap.put("player", (p, v, m) -> Optional.of(Nucleus.getNucleus().getTextParsingUtils().addCommandToDisplayName(getFromVariableIfExists(p, v, m))));
        translatorMap.put("playername", (p, v, m) -> Optional.of(Nucleus.getNucleus().getTextParsingUtils().addCommandToDisplayName(getFromVariableIfExists(p, v, m))));

        translatorMap.put("prefix", (p, v, m) -> getTextFromOption(getFromVariableIfExists(p, v, m), "prefix"));
        translatorMap.put("suffix", (p, v, m) -> getTextFromOption(getFromVariableIfExists(p, v, m), "suffix"));

        translatorMap.put("playerdisplayname", (p, v, m) -> Optional.of(Nucleus.getNucleus().getTextParsingUtils().addCommandToDisplayName(getFromVariableIfExists(p, v, m))));
        translatorMap.put("displayname", (p, v, m) -> Optional.of(Nucleus.getNucleus().getTextParsingUtils().addCommandToDisplayName(getFromVariableIfExists(p, v, m))));

        translatorMap.put("maxplayers", (p, v, m) -> Optional.of(Text.of(Sponge.getServer().getMaxPlayers())));
        translatorMap.put("onlineplayers", (p, v, m) -> Optional.of(Text.of(Sponge.getServer().getOnlinePlayers().size())));
        translatorMap.put("currentworld", (p, v, m) -> Optional.of(Text.of(getWorld(getFromVariableIfExists(p, v, m)).getName())));
        translatorMap.put("time", (p, v, m) -> Optional.of(Text.of(String.valueOf(Util
                .getTimeFromTicks(getWorld(getFromVariableIfExists(p, v, m)).getProperties().getWorldTime())))));

        translatorMap.put("uniquevisitor", (p, v, m) -> Optional.of(Text.of(Nucleus.getNucleus()
                .getGeneralService().getTransient(UniqueUserCountTransientModule.class).getUniqueUserCount())));

        translatorMap.put("ipaddress", (p, v, m) -> Optional.of(Text.of(p instanceof RemoteSource ?
            ((RemoteSource)p).getConnection().getAddress().getAddress().toString() :
            "localhost")));

        translatorMap.put("subject", (p, v, m) -> Optional.of(Text.of((p instanceof ConsoleSource ? "-" : p.getName()))));
    }

    @Nonnull @Override public Optional<Text> parse(String tokenInput, CommandSource source, Map<String, Object> variables) {
        // Token
        String[] split = tokenInput.split("\\|", 2);
        String var = "";
        if (split.length == 2) {
            var = split[1];
        }

        return translatorMap.getOrDefault(split[0].toLowerCase(), (p, v, m) -> Optional.empty()).get(source, var, variables);
    }

    public Set<String> getTokenNames() {
        return Sets.newHashSet(translatorMap.keySet());
    }

    private static CommandSource getFromVariableIfExists(CommandSource source, String v, Map<String, Object> m) {
        if (m.containsKey(v) && m.get(v) instanceof CommandSource) {
            return (CommandSource)m.get(v);
        }

        return source;
    }

    private static World getWorld(CommandSource p) {
        World world;
        if (p instanceof Locatable) {
            world = ((Locatable) p).getWorld();
        } else {
            world = Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get();
        }

        return world;
    }

    private static Optional<Text> getTextFromOption(CommandSource cs, String option) {
        return Util.getOptionFromSubject(cs, option)
                .map(x -> x.isEmpty() ? null : TextSerializers.FORMATTING_CODE.deserialize(x));
    }

    @FunctionalInterface
    private interface Translator {

        Optional<Text> get(CommandSource source, String variableString, Map<String, Object> variables);
    }
}
