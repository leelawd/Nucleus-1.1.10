/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.World;

import javax.inject.Inject;

public class AFKMoveOnlyListener extends AbstractAFKListener implements ListenerBase.Conditional {

    @Inject
    public AFKMoveOnlyListener(AFKHandler handler) {
        super(handler);
    }

    @Listener(order = Order.LAST)
    public void onPlayerMove(final MoveEntityEvent event, @Root Player player,
            @Getter("getFromTransform") Transform<World> from,
            @Getter("getToTransform") Transform<World> to) {
        if (!from.getPosition().equals(to.getPosition())) {
            update(player);
        }
    }

    @Override
    public boolean shouldEnable() {
        return getTriggerConfigEntry(t -> t.isOnMovement() && !t.isOnRotation());
    }

}
