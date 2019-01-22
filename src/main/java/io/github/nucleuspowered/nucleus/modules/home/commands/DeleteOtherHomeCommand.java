/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.handlers.HomeHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@Permissions(prefix = "home", mainOverride = "deleteother")
@NoModifiers
@RegisterCommand({"deletehomeother", "delhomeother"})
@NonnullByDefault
public class DeleteOtherHomeCommand extends AbstractCommand<CommandSource> {

    private final String homeKey = "home";

    private final CoreConfigAdapter cca;
    private final HomeHandler homeHandler;

    @Inject
    public DeleteOtherHomeCommand(CoreConfigAdapter cca, HomeHandler homeHandler) {
        this.cca = cca;
        this.homeHandler = homeHandler;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new HomeOtherArgument(Text.of(homeKey), plugin, cca))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Home wl = args.<Home>getOne(homeKey).get();

        homeHandler.removeHomeInternal(Cause.of(NamedCause.owner(src)), wl);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.delete.other.success", wl.getUser().getName(), wl.getName()));
        return CommandResult.success();
    }
}
