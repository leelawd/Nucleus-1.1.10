/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.freezeplayer.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.datamodules.FreezePlayerUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

public class FreezePlayerListener extends ListenerBase {

    @Inject private UserDataManager ucl;

    private final Map<UUID, Instant> lastFreezeNotification = Maps.newHashMap();

    @Listener
    public void onPlayerMovement(MoveEntityEvent event, @Root Player player) {
        event.setCancelled(checkForFrozen(player, "freeze.cancelmove"));
    }

    @Listener
    public void onPlayerInteractBlock(InteractEvent event, @Root Player player) {
        event.setCancelled(checkForFrozen(player, "freeze.cancelinteract"));
    }

    @Listener
    public void onPlayerInteractBlock(InteractBlockEvent event, @Root Player player) {
        event.setCancelled(checkForFrozen(player, "freeze.cancelinteractblock"));
    }

    private boolean checkForFrozen(Player player, String message) {
        if (ucl.getUser(player).get().quickGet(FreezePlayerUserDataModule.class, FreezePlayerUserDataModule::isFrozen)) {
            Instant now = Instant.now();
            if (lastFreezeNotification.getOrDefault(player.getUniqueId(), now).isBefore(now)) {
                player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat(message));
                lastFreezeNotification.put(player.getUniqueId(), now.plus(2, ChronoUnit.SECONDS));
            }

            return true;
        }

        return false;
    }
}
