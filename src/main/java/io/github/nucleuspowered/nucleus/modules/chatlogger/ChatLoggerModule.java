/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger;

import static io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule.ID;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = ID, name = "Chat Logger")
public class ChatLoggerModule extends ConfigurableModule<ChatLoggingConfigAdapter> {

    public static final String ID = "chat-logger";

    @Override public ChatLoggingConfigAdapter createAdapter() {
        return new ChatLoggingConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        ChatLoggerHandler clh = new ChatLoggerHandler(plugin, getAdapter());
        serviceManager.registerService(ChatLoggerHandler.class, clh);
        plugin.registerReloadable(clh::onReload);
        clh.onReload();
    }
}
