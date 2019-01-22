/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

@Permissions(supportsOthers = true)
@RegisterCommand({"lightning", "strike", "smite", "thor", "shock"})
@EssentialsEquivalent(value = {"lightning", "strike", "smite", "thor", "shock"}, isExact = false,
        notes = "Selectors can be used, entities can be struck.")
public class LightningCommand extends AbstractCommand<CommandSource> {

    private final String player = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.optional(
                    GenericArguments.requiringPermission(
                        SelectorWrapperArgument.nicknameSelector(Text.of(player), NicknameArgument.UnderlyingType.PLAYER, false, Living.class),
                            permissions.getPermissionWithSuffix("others")))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Collection<Living> playerCollection = args.getAll(player);

        // No argument, let's not smite the subject.
        if (playerCollection.isEmpty()) {
            if (!(src instanceof Player)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.playeronly"));
                return CommandResult.empty();
            }

            Player pl = (Player)src;

            // 100 is a good limit here.
            BlockRay<World> playerBlockRay = BlockRay.from(pl).distanceLimit(100).stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1)).build();
            Optional<BlockRayHit<World>> obh = playerBlockRay.end();
            Location<World> lightningLocation;
            // Smite above, but not on.
            lightningLocation = obh.map(BlockRayHit::getLocation).orElseGet(() -> pl.getLocation().add(0, 3, 0));

            return this.spawnLightning(lightningLocation, src, null);
        }

        int successCount = 0;
        for (Living pl : playerCollection) {
            CommandResult cr = this.spawnLightning(
                    pl.getLocation(),
                    src,
                    pl instanceof Player ? (Player)pl : null);
            successCount += cr.getSuccessCount().orElse(0);
        }

        return CommandResult.builder().successCount(successCount).build();
    }

    private CommandResult spawnLightning(Location<World> location, CommandSource src, @Nullable Player target) {
        World world = location.getExtent();
        Entity bolt = world.createEntity(EntityTypes.LIGHTNING, location.getPosition());

        Cause cause = Cause.of(
                NamedCause.owner(SpawnCause.builder().type(SpawnTypes.PLUGIN).build()),
                NamedCause.source(src));

        if (world.spawnEntity(bolt, cause)) {
            if (target != null) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithTextFormat("command.lightning.success.other", plugin.getNameUtil()
                        .getName(target)));
            }

            return CommandResult.success();
        }

        if (target != null) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithTextFormat("command.lightning.errorplayer", plugin.getNameUtil()
                    .getName(target)));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.lightning.error"));
        }

        return CommandResult.empty();
    }
}
