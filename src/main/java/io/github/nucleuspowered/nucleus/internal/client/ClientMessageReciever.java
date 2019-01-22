/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.client;

import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public class ClientMessageReciever implements MessageReceiver {

    @Override
    public void sendMessage(Text message) {
        Nucleus.getNucleus().getLogger().info(message.toPlain());
    }

    @Override
    public MessageChannel getMessageChannel() {
        return MessageChannel.TO_ALL;
    }

    @Override
    public void setMessageChannel(MessageChannel channel) {
        // noop
    }
}
