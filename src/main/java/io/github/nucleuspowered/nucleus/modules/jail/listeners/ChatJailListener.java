/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.jail.JailModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import java.util.function.Predicate;

import javax.inject.Inject;

@ConditionalListener(ChatJailListener.Condition.class)
public class ChatJailListener extends ListenerBase {

    @Inject private JailHandler handler;

    @Listener(order = Order.FIRST)
    public void onChat(MessageChannelEvent.Chat event, @Root Player player) {
        if (handler.checkJail(player, false)) {
            player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("jail.muteonchat"));
            event.setCancelled(true);
        }
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            return nucleus.getConfigValue(JailModule.ID, JailConfigAdapter.class, JailConfig::isMuteOnJail).orElse(false);
        }
    }
}
