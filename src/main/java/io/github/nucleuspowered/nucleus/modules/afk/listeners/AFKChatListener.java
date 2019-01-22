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
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;

import javax.inject.Inject;

public class AFKChatListener extends AbstractAFKListener implements ListenerBase.Conditional {

    @Inject
    public AFKChatListener(AFKHandler handler) {
        super(handler);
    }

    @Listener
    public void onPlayerChat(final MessageChannelEvent.Chat event, @Root Player player) {
        update(player);
    }

    @Override
    public boolean shouldEnable() {
        return getTriggerConfigEntry(AFKConfig.Triggers::isOnChat);
    }
}
