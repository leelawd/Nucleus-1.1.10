/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.listeners;

import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.modules.afk.commands.AFKCommand;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class AFKCommandListener extends AbstractAFKListener implements ListenerBase.Conditional {
    private final List<String> commands;

    @Inject
    public AFKCommandListener(AFKHandler handler) {
        super(handler);
        this.commands = Arrays.stream(AFKCommand.class.getAnnotation(RegisterCommand.class).value()).map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    @Listener
    public void onPlayerCommand(final SendCommandEvent event, @Root Player player) {
        // Did the subject run /afk? Then don't do anything, we'll toggle it
        // anyway.
        if (!commands.contains(event.getCommand().toLowerCase())) {
            update(player);
        }
    }

    @Override
    public boolean shouldEnable() {
        return getTriggerConfigEntry(AFKConfig.Triggers::isOnCommand);
    }
}
