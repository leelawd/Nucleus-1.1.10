/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.config.HomeConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.events.UseHomeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@Permissions(prefix = "home", mainOverride = "other", suggestedLevel = SuggestedLevel.MOD)
@RegisterCommand("homeother")
public class HomeOtherCommand extends AbstractCommand<Player> {

    private final String home = "home";
    public static final String OTHER_EXEMPT_PERM_SUFFIX = "exempt.target";

    @Inject private CoreConfigAdapter cca;
    @Inject private HomeConfigAdapter homeConfigAdapter;

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("exempt.target", PermissionInformation.getWithTranslation("permission.home.other.exempt.target", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new HomeOtherArgument(Text.of(home), plugin, cca))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Home wl = args.<Home>getOne(home).get();

        Sponge.getServer().loadWorld(wl.getWorldProperties()
                .orElseThrow(() -> ReturnMessageException.fromKey("command.home.invalid", wl.getName())));

        Location<World> targetLocation = wl.getLocation().orElseThrow(() -> ReturnMessageException.fromKey("command.home.invalid", wl.getName()));

        UseHomeEvent event = new UseHomeEvent(Cause.of(NamedCause.owner(src)), src, wl);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                plugin.getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        // Warp to it safely.
        if (plugin.getTeleportHandler().teleportPlayer(src, targetLocation, wl.getRotation(), homeConfigAdapter.getNodeOrDefault()
                .isSafeTeleport()).isSuccess()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.homeother.success", wl.getUser().getName(), wl.getName()));
            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.homeother.fail", wl.getUser().getName(), wl.getName());
        }
    }
}
