/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;

@Permissions(prefix = "nucleus", suggestedLevel = SuggestedLevel.NONE)
@RunAsync
@NoModifiers
@RegisterCommand(value = "rebuildusercache", subcommandOf = NucleusCommand.class)
public class RebuildUserCacheCommand extends AbstractCommand<CommandSource> {

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.rebuild.start"));
        if (plugin.getUserCacheService().fileWalk()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.rebuild.end"));
            return CommandResult.success();
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.rebuild.fail"));
            return CommandResult.empty();
        }
    }
}
