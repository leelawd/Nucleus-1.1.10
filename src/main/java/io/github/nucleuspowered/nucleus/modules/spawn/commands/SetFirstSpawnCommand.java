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
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@RegisterCommand({"setfirstspawn"})
@Permissions(prefix = "firstspawn", mainOverride = "set")
@NoModifiers
@RunAsync
@NonnullByDefault
public class SetFirstSpawnCommand extends AbstractCommand<Player> {

    private final ModularGeneralService data;

    @Inject
    public SetFirstSpawnCommand(ModularGeneralService data) {
        this.data = data;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        data.get(SpawnGeneralDataModule.class).setFirstSpawn(src.getLocation(), src.getRotation());
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.setfirstspawn.success"));
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.setfirstspawn.success2"));

        return CommandResult.success();
    }

}
