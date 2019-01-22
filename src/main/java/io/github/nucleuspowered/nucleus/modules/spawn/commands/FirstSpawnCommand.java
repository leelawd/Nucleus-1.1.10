/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.inject.Inject;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("firstspawn")
public class FirstSpawnCommand extends AbstractCommand<Player> {

    @Inject private ModularGeneralService data;
    @Inject private SpawnConfigAdapter spawnConfigAdapter;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {

        Optional<Transform<World>> olwr = data.get(SpawnGeneralDataModule.class).getFirstSpawn();
        if (!olwr.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstspawn.notset"));
            return CommandResult.empty();
        }

        if (plugin.getTeleportHandler().teleportPlayer(src, olwr.get(), spawnConfigAdapter.getNodeOrDefault().isSafeTeleport()).isSuccess()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstspawn.success"));
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.firstspawn.fail"));
        return CommandResult.empty();
    }
}
