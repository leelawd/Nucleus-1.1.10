/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

/**
 * plugin.firstspawn.remove.base
 */
@RegisterCommand(value = {"del", "rm"}, subcommandOf = SetFirstSpawnCommand.class)
@Permissions(prefix = "firstspawn", mainOverride = "remove")
@NoModifiers
@RunAsync
@NonnullByDefault
public class RemoveFirstSpawnCommand extends AbstractCommand<CommandSource> {

    private final ModularGeneralService data;

    @Inject
    public RemoveFirstSpawnCommand(ModularGeneralService data) {
        this.data = data;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        data.get(SpawnGeneralDataModule.class).removeFirstSpawn();
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.setfirstspawn.remove"));
        return CommandResult.success();
    }
}
