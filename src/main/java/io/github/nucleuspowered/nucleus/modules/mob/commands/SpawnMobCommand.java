/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mob.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedCatalogTypeArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mob.config.BlockSpawnsConfig;
import io.github.nucleuspowered.nucleus.modules.mob.config.MobConfig;
import io.github.nucleuspowered.nucleus.modules.mob.config.MobConfigAdapter;
import org.spongepowered.api.CatalogTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Permissions(supportsOthers = true)
@RegisterCommand({"spawnmob", "spawnentity", "mobspawn"})
@EssentialsEquivalent({"spawnmob", "mob"})
public class SpawnMobCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    private final String amountKey = "amount";
    private final String mobTypeKey = "mob";

    @Inject private MobConfigAdapter mobConfigAdapter;

    @Override public CommandElement[] additionalArguments() {
        return new CommandElement[] {
                new ImprovedCatalogTypeArgument(Text.of(mobTypeKey), CatalogTypes.ENTITY_TYPE),
                GenericArguments.optional(new PositiveIntegerArgument(Text.of(amountKey)), 1)
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("mob", PermissionInformation.getWithTranslation("permission.spawnmob.mob", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeWithPlayer(CommandSource src, Player pl, CommandContext args, boolean isSelf) throws Exception {
        // Get the amount
        int amount = args.<Integer>getOne(amountKey).get();
        EntityType et = args.<EntityType>getOne(mobTypeKey).get();

        if (!Living.class.isAssignableFrom(et.getEntityClass())) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnmob.livingonly", et.getTranslation().get()));
        }

        MobConfig mc = mobConfigAdapter.getNodeOrDefault();
        String id = et.getId().toLowerCase();
        if (mc.isPerMobPermission() && !permissions.testSuffix(src, "mob." + id.replace(":", "."))) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnmob.mobnoperm", et.getTranslation().get()));
        }

        Optional<BlockSpawnsConfig> config = mc.getBlockSpawnsConfigForWorld(pl.getWorld());
        if (config.isPresent() && (config.get().isBlockVanillaMobs() && id.startsWith("minecraft:") || config.get().getIdsToBlock().contains(id))) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.spawnmob.blockedinconfig", et.getTranslation().get()));
        }

        Location<World> loc = pl.getLocation();
        World w = loc.getExtent();
        MessageProvider mp = plugin.getMessageProvider();

        // Count the number of entities spawned.
        int i = 0;

        // Sponge requires the root cause to be a SpawnCause. So we don't lose sight of the subject causing this,
        // we make use of Sponge's awesome Cause system, and just make them the second argument.
        Cause cause = Cause.of(
                NamedCause.source(EntitySpawnCause.builder().type(SpawnTypes.PLUGIN).entity(pl).build()),
                NamedCause.owner(pl));
        Entity entityone = null;
        do {
            Entity e = w.createEntity(et, loc.getPosition());
            if (!w.spawnEntity(e, cause)) {
                throw ReturnMessageException.fromKeyText("command.spawnmob.fail", Text.of(e));
            }

            if (entityone == null) {
                entityone = e;
            }

            i++;
        } while (i < Math.min(amount, mobConfigAdapter.getNodeOrDefault().getMaxMobsToSpawn()));

        if (amount > mobConfigAdapter.getNodeOrDefault().getMaxMobsToSpawn()) {
            src.sendMessage(
                    mp.getTextMessageWithFormat("command.spawnmob.limit", String.valueOf(mobConfigAdapter.getNodeOrDefault().getMaxMobsToSpawn())));
        }

        if (i == 0) {
            throw ReturnMessageException.fromKey("command.spawnmob.fail", et.getTranslation().get());
        }

        if (i == 1) {
            src.sendMessage(mp.getTextMessageWithTextFormat("command.spawnmob.success.singular", Text.of(i), Text.of(entityone)));
        } else {
            src.sendMessage(mp.getTextMessageWithTextFormat("command.spawnmob.success.plural", Text.of(i), Text.of(entityone)));
        }

        return CommandResult.success();
    }
}
