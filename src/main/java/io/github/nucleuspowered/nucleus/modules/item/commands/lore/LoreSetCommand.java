/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

@Permissions(prefix = "lore")
@RegisterCommand(value = "set", subcommandOf = LoreCommand.class)
public class LoreSetCommand extends LoreSetBaseCommand {

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        return setLore(src, args.<String>getOne(loreKey).get(), true);
    }
}
