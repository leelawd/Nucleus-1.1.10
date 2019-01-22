/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveDoubleArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.WarpArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@RunAsync
@NoModifiers
@NonnullByDefault
@Permissions(prefix = "warp")
@RegisterCommand(value = {"cost", "setcost"}, subcommandOf = WarpCommand.class)
public class SetCostCommand extends AbstractCommand<CommandSource> {

    private final WarpConfigAdapter warpConfigAdapter;
    private final WarpHandler warpHandler;
    private final String warpKey = "warp";
    private final String costKey = "cost";

    @Inject
    public SetCostCommand(WarpConfigAdapter warpConfigAdapter, WarpHandler warpHandler) {
        this.warpConfigAdapter = warpConfigAdapter;
        this.warpHandler = warpHandler;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new WarpArgument(Text.of(warpKey), warpConfigAdapter, false)),
            GenericArguments.onlyOne(new PositiveDoubleArgument(Text.of(costKey)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Warp warpData = args.<Warp>getOne(warpKey).get();
        double cost = args.<Double>getOne(costKey).get();
        if (cost < -1) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.costset.arg"));
            return CommandResult.empty();
        }

        if (cost == -1 && warpHandler.setWarpCost(warpData.getName(), -1)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.costset.reset", warpData.getName(), String.valueOf(warpConfigAdapter.getNodeOrDefault().getDefaultWarpCost())));
            return CommandResult.success();
        } else if (warpHandler.setWarpCost(warpData.getName(), cost)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.costset.success", warpData.getName(), String.valueOf(cost)));
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warp.costset.failed", warpData.getName()));
        return CommandResult.empty();
    }
}
