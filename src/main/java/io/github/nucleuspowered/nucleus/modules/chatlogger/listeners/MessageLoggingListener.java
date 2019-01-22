/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusMessageEvent;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfig;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.util.function.Predicate;

import javax.inject.Inject;

@ConditionalListener(MessageLoggingListener.Condition.class)
public class MessageLoggingListener extends ListenerBase {

    @Inject private ChatLoggerHandler handler;

    @Listener(order = Order.LAST)
    public void onCommand(NucleusMessageEvent event) {
        String message = plugin.getMessageProvider().getMessageWithFormat("chatlog.message",
            event.getSender().getName(), event.getRecipient().getName(), event.getMessage());
        handler.queueEntry(message);
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            try {
                ChatLoggingConfig c = nucleus.getModuleContainer().getConfigAdapterForModule(ChatLoggerModule.ID, ChatLoggingConfigAdapter.class)
                    .getNodeOrDefault();
                return c.isEnableLog() && c.isLogMessages();
            } catch (NoModuleException | IncorrectAdapterTypeException e) {
                if (nucleus.isDebugMode()) {
                    e.printStackTrace();
                }

                return false;
            }
        }
    }
}
