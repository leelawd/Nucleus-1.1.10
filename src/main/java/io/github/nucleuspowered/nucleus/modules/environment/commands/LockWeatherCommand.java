/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.commands;

import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.environment.datamodule.EnvironmentWorldDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Optional;

import javax.inject.Inject;

@Permissions
@RunAsync
@RegisterCommand({ "lockweather", "killweather" })
@NoModifiers
@NonnullByDefault
public class LockWeatherCommand extends AbstractCommand<CommandSource> {

    private final WorldDataManager loader;

    private final String worldKey = "world";
    private final String toggleKey = "toggle";

    @Inject
    public LockWeatherCommand(WorldDataManager loader) {
        this.loader = loader;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.world(Text.of(worldKey)))),
                GenericArguments.onlyOne(GenericArguments.optional(GenericArguments.bool(Text.of(toggleKey))))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<WorldProperties> world = getWorldProperties(src, worldKey, args);
        if (!world.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.specifyworld"));
            return CommandResult.empty();
        }

        WorldProperties wp = world.get();
        Optional<ModularWorldService> ws = loader.getWorld(wp.getUniqueId());
        if (!ws.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.noworld", wp.getWorldName()));
            return CommandResult.empty();
        }

        EnvironmentWorldDataModule environmentWorldDataModule = ws.get().get(EnvironmentWorldDataModule.class);
        boolean toggle = args.<Boolean>getOne(toggleKey).orElse(!environmentWorldDataModule.isLockWeather());

        environmentWorldDataModule.setLockWeather(toggle);
        ws.get().set(environmentWorldDataModule);
        if (toggle) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.lockweather.locked", wp.getWorldName()));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.lockweather.unlocked", wp.getWorldName()));
        }

        return CommandResult.success();
    }
}
