/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.runnables;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfig;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

public class ChatLoggerRunnable extends TaskBase {

    private final ChatLoggingConfigAdapter clca;
    private final ChatLoggerHandler handler;
    private ChatLoggingConfig config = null;

    @Inject
    public ChatLoggerRunnable(NucleusPlugin plugin, ChatLoggingConfigAdapter clca, ChatLoggerHandler handler) {
        plugin.registerReloadable(() -> config = null);
        this.clca = clca;
        this.handler = handler;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(1, ChronoUnit.SECONDS);
    }


    @Override
    public void accept(Task task) {
        if (Sponge.getGame().getState() == GameState.SERVER_STOPPED) {
            return;
        }

        if (config == null) {
            config = clca.getNodeOrDefault();
        }

        if (config.isEnableLog()) {
            handler.onTick();
        }
    }
}
