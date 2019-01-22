/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.WeatherArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.environment.config.EnvironmentConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.environment.datamodule.EnvironmentWorldDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.weather.Weather;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Permissions
@RegisterCommand("weather")
@EssentialsEquivalent({"thunder", "sun", "weather", "sky", "storm", "rain"})
public class WeatherCommand extends AbstractCommand<CommandSource> {
    private final String world = "world";
    private final String weather = "weather";
    private final String duration = "duration";

    @Inject private WorldDataManager loader;
    @Inject private EnvironmentConfigAdapter eca;

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.length", PermissionInformation.getWithTranslation("permission.weather.exempt.length", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.optionalWeak(GenericArguments.onlyOne(
                        new NucleusWorldPropertiesArgument(Text.of(world), NucleusWorldPropertiesArgument.Type.ENABLED_ONLY))),
                GenericArguments.onlyOne(new WeatherArgument(Text.of(weather))), // More flexible with the arguments we can use.
                GenericArguments.onlyOne(GenericArguments.optional(new TimespanArgument(Text.of(duration))))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // We can predict the weather on multiple worlds now!
        WorldProperties wp = this.getWorldFromUserOrArgs(src, world, args);
        World w = Sponge.getServer().getWorld(wp.getUniqueId())
            .orElseThrow(() -> ReturnMessageException.fromKey("args.worldproperties.notloaded", wp.getWorldName()));

        // Get whether we locked the weather.
        ModularWorldService ew = loader.getWorld(w).get();
        if (ew.quickGet(EnvironmentWorldDataModule.class, EnvironmentWorldDataModule::isLockWeather)) {
            // Tell the user to unlock first.
            throw ReturnMessageException.fromKey("command.weather.locked", w.getName());
        }

        // Houston, we have a world! Now, what was the forecast?
        Weather we = args.<Weather>getOne(weather).get();

        // Have we gotten an accurate forecast? Do we know how long this weather spell will go on for?
        Optional<Long> oi = args.getOne(duration);

        // Even weather masters have their limits. Sometimes.
        long max = eca.getNodeOrDefault().getMaximumWeatherTimespan();
        if (max > 0 && oi.orElse(Long.MAX_VALUE) > max && !permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.weather.toolong", Util.getTimeStringFromSeconds(max));
        }

        if (oi.isPresent()) {
            // YES! I should get a job at the weather service and show them how it's done!
            w.setWeather(we, oi.get());
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.weather.time", we.getName(), w.getName(), Util.getTimeStringFromSeconds(oi.get())));
        } else {
            // No, probably because I've already gotten a job at the weather service...
            w.setWeather(we);
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.weather.set", we.getName(), w.getName()));
        }

        // The weather control device has been activated!
        return CommandResult.success();
    }
}
