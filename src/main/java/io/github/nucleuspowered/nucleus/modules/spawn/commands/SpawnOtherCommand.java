/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.spawn.config.GlobalSpawnConfig;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.spawn.events.SendToSpawnEvent;
import io.github.nucleuspowered.nucleus.modules.spawn.helpers.SpawnHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@NoModifiers
@NonnullByDefault
@Permissions(prefix = "spawn", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = "other", subcommandOf = SpawnCommand.class)
public class SpawnOtherCommand extends AbstractCommand<CommandSource> {

    private final String otherKey = "subject";
    private final String worldKey = "world";
    private final SpawnConfigAdapter sca;

    @Inject
    public SpawnOtherCommand(SpawnConfigAdapter sca) {
        this.sca = sca;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.user(Text.of(otherKey)),
            GenericArguments.optional(GenericArguments.world(Text.of(worldKey)))
        };
    }

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("offline", PermissionInformation.getWithTranslation("permission.spawnother.offline", SuggestedLevel.ADMIN));
        }};
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User target = args.<User>getOne(otherKey).get();
        GlobalSpawnConfig gsc = sca.getNodeOrDefault().getGlobalSpawn();
        WorldProperties world = this.getWorldProperties(src, worldKey, args)
            .orElseGet(() -> gsc.isOnSpawnCommand() ? gsc.getWorld().get() : Sponge.getServer().getDefaultWorld().get());

        Transform<World> worldTransform = SpawnHelper.getSpawn(world, plugin, target.getPlayer().orElse(null));

        SendToSpawnEvent event = new SendToSpawnEvent(worldTransform, target, Cause.of(NamedCause.source(src), NamedCause.owner(plugin)));
        if (Sponge.getEventManager().post(event)) {
            if (event.getCancelReason().isPresent()) {
                throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnother.other.failed.reason", target.getName(), event.getCancelReason().get()));
            }

            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnother.other.failed.noreason", target.getName()));
        }

        if (!target.isOnline()) {
            return isOffline(src, target, worldTransform);
        }

        // If we don't have a rotation, then use the current rotation
        Player player = target.getPlayer().get();
        NucleusTeleportHandler.TeleportResult result = plugin.getTeleportHandler().teleportPlayer(player, worldTransform, sca.getNodeOrDefault()
                .isSafeTeleport());
        if (result.isSuccess()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnother.success.source", target.getName(), world.getWorldName()));
            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnother.success.target", world.getWorldName()));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.spawnother.fail", target.getName(), world.getWorldName());
    }

    private CommandResult isOffline(CommandSource source, User user, Transform<World> worldTransform) throws Exception {
        if (!permissions.testSuffix(source, "offline")) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnother.offline.permission"));
        }

        plugin.getUserDataManager().get(user).get().get(CoreUserDataModule.class).sendToLocationOnLogin(worldTransform.getLocation());
        source.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnother.offline.sendonlogin", user.getName(), worldTransform.getExtent().getName()));
        return CommandResult.success();
    }
}
