/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.chatlogger.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.chatlogger.ChatLoggerModule;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfig;
import io.github.nucleuspowered.nucleus.modules.chatlogger.config.ChatLoggingConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.chatlogger.handlers.ChatLoggerHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.util.function.Predicate;

import javax.inject.Inject;

@ConditionalListener(ChatLoggingListener.Condition.class)
public class ChatLoggingListener extends ListenerBase {

    @Inject private ChatLoggerHandler handler;

    @Listener(order = Order.LAST)
    public void onCommand(MessageChannelEvent.Chat event, @First CommandSource source) {
        String message = plugin.getMessageProvider().getMessageWithFormat("chatlog.chat", source.getName(), event.getMessage().toPlain());
        handler.queueEntry(message);
    }

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event, @First CommandSource source) {
        if (event.getCommand().equalsIgnoreCase("say") || event.getCommand().equalsIgnoreCase("minecraft:say")) {
            String message = plugin.getMessageProvider().getMessageWithFormat("chatlog.chat", source.getName(), event.getArguments());
            handler.queueEntry(message);
        }
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            try {
                ChatLoggingConfig c = nucleus.getModuleContainer().getConfigAdapterForModule(ChatLoggerModule.ID, ChatLoggingConfigAdapter.class)
                    .getNodeOrDefault();
                return c.isEnableLog() && c.isLogChat();
            } catch (NoModuleException | IncorrectAdapterTypeException e) {
                if (nucleus.isDebugMode()) {
                    e.printStackTrace();
                }

                return false;
            }
        }
    }
}
