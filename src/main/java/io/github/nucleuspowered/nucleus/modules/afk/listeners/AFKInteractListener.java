/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import javax.inject.Inject;

public class AFKInteractListener extends AbstractAFKListener implements ListenerBase.Conditional {

    @Inject
    public AFKInteractListener(AFKHandler handler) {
        super(handler);
    }

    @Listener(order = Order.LAST)
    public void onPlayerInteract(final InteractEvent event, @Root Player player) {
        update(player);
    }

    @Override
    public boolean shouldEnable() {
        return getTriggerConfigEntry(AFKConfig.Triggers::isOnInteract);
    }
}
