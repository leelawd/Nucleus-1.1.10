/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnWorldDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"teleport", "tp"}, subcommandOf = WorldCommand.class)
@EssentialsEquivalent(value = "world", notes = "The world command in Essentials was just a warp command.")
public class TeleportWorldCommand extends AbstractCommand<CommandSource> {

    private final String world = "world";
    private final String playerKey = "subject";

    @Inject
    private WorldDataManager worldDataManager;

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("others", PermissionInformation.getWithTranslation("permission.world.teleport.other", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            new NucleusWorldPropertiesArgument(Text.of(world), NucleusWorldPropertiesArgument.Type.ENABLED_ONLY),
            GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.player(Text.of(playerKey))),
                permissions.getPermissionWithSuffix("others")
            ))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Player player = getUserFromArgs(Player.class, src, playerKey, args, "command.world.player");
        WorldProperties worldProperties = args.<WorldProperties>getOne(world).get();
        if (!worldProperties.isEnabled()) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.world.teleport.notenabled", worldProperties.getWorldName()));
        }

        World world = Sponge.getServer().loadWorld(worldProperties.getUniqueId())
            .orElseThrow(() -> ReturnMessageException.fromKey(
                    "command.world.teleport.failed", worldProperties.getWorldName()
            ));

        if (!player.transferToWorld(world, worldProperties.getSpawnPosition().toDouble())) {
            throw ReturnMessageException.fromKey(
                    "command.world.teleport.failed", worldProperties.getWorldName());
        }

        // Rotate.
        worldDataManager.getWorld(worldProperties.getUniqueId()).ifPresent(x -> x.quickGet(SpawnWorldDataModule.class, SpawnWorldDataModule::getSpawnRotation)
            .ifPresent(player::setRotation));
        if (src instanceof Player && ((Player) src).getUniqueId().equals(player.getUniqueId())) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.teleport.success", worldProperties.getWorldName()));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.teleport.successplayer",
                plugin.getNameUtil().getSerialisedName(player), worldProperties.getWorldName()));
            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.teleport.success", worldProperties.getWorldName()));
        }

        return CommandResult.success();
    }
}
