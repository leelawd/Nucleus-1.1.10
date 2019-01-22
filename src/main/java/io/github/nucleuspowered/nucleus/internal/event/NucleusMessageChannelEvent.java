/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.event;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;

import java.util.Optional;

import javax.annotation.Nullable;

public class NucleusMessageChannelEvent extends AbstractEvent implements MessageChannelEvent.Chat {

    private final Cause cause;
    private final MessageChannel original;
    private final Text originalMessage;
    @Nullable private MessageChannel messageChannel;
    private final Text rawMessage;
    private MessageFormatter formatters;
    private boolean isCancelled = false;
    private boolean messageCancelled = false;

    public NucleusMessageChannelEvent(Cause cause, MessageChannel original, Text rawMessage, MessageFormatter formatters) {
        this.cause = cause;
        this.original = original;
        this.messageChannel = original;
        this.rawMessage = rawMessage;
        this.formatters = formatters;
        this.originalMessage = formatters.toText();
    }

    @Override public Text getRawMessage() {
        return rawMessage;
    }

    @Override public boolean isCancelled() {
        return isCancelled;
    }

    @Override public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override public MessageChannel getOriginalChannel() {
        return original;
    }

    @Override public Optional<MessageChannel> getChannel() {
        return Optional.ofNullable(messageChannel);
    }

    @Override public void setChannel(@Nullable MessageChannel channel) {
        messageChannel = channel;
    }

    @Override public Text getOriginalMessage() {
        return originalMessage;
    }

    @Override public boolean isMessageCancelled() {
        return messageCancelled;
    }

    @Override public void setMessageCancelled(boolean cancelled) {
        messageCancelled = cancelled;
    }

    @Override public MessageFormatter getFormatter() {
        return formatters;
    }

    @Override public Cause getCause() {
        return cause;
    }
}
