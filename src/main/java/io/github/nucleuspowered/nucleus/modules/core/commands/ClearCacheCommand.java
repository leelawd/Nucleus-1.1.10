/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;

import javax.inject.Inject;

/**
 * Clears the {@link UserDataManager} cache, so any offline user's files wll be read on next startup.
 */
@SuppressWarnings("ALL")
@Permissions(prefix = "nucleus")
@RunAsync
@NoModifiers
@RegisterCommand(value = "clearcache", subcommandOf = NucleusCommand.class)
public class ClearCacheCommand extends AbstractCommand<CommandSource> {

    @Inject private UserDataManager ucl;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("f").buildWith(GenericArguments.none())
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean force = args.hasAny("f");
        ucl.removeOfflinePlayers(force);

        if (force) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.clearcache.success"));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.nucleus.clearcache.success2m"));
        }

        return CommandResult.success();
    }
}