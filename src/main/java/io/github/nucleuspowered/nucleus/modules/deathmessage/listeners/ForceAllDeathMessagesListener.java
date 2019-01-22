/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.deathmessage.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.deathmessage.DeathMessageModule;
import io.github.nucleuspowered.nucleus.modules.deathmessage.config.DeathMessageConfigAdapter;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.function.Predicate;

@ConditionalListener(ForceAllDeathMessagesListener.Condition.class)
public class ForceAllDeathMessagesListener extends ListenerBase {

    @Listener(order = Order.LATE)
    public void onDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Living living) {
        if (living instanceof Player) {
            event.setChannel(MessageChannel.TO_ALL);
        }
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            return nucleus
                .getConfigValue(DeathMessageModule.ID, DeathMessageConfigAdapter.class, x -> x.isEnableDeathMessages() && x.isForceForAll())
                .orElse(false);
        }
    }
}