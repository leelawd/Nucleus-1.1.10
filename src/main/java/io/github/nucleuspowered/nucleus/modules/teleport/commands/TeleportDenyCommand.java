/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

/**
 * /tpdeny.
 */
@Permissions(prefix = "teleport", suggestedLevel = SuggestedLevel.USER)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"tpdeny", "teleportdeny", "tpno"})
@EssentialsEquivalent({"tpdeny", "tpno"})
public class TeleportDenyCommand extends AbstractCommand<Player> {

    private final TeleportHandler teleportHandler;

    @Inject
    public TeleportDenyCommand(TeleportHandler teleportHandler) {
        this.teleportHandler = teleportHandler;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        boolean denied = teleportHandler.remove(src.getUniqueId());
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat(denied ? "command.tpdeny.deny" : "command.tpdeny.fail"));
        return CommandResult.success();
    }
}
