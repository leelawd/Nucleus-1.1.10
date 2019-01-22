/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.handlers;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.logging.AbstractLoggingHandler;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;

import javax.inject.Inject;

public class ChatLoggerHandler extends AbstractLoggingHandler {

    private final ChatLoggingConfigAdapter clca;

    @Inject
    public ChatLoggerHandler(NucleusPlugin plugin, ChatLoggingConfigAdapter clca) {
        super(plugin, "chat", "chat");
        this.clca = clca;
    }

    public void onReload() throws Exception {
        if (clca.getNodeOrDefault().isEnableLog() && logger == null) {
            this.createLogger();
        } else if (!clca.getNodeOrDefault().isEnableLog() && logger != null) {
            onShutdown();
        }
    }

    @Override protected boolean enabledLog() {
        return clca.getNodeOrDefault().isEnableLog();
    }
}
