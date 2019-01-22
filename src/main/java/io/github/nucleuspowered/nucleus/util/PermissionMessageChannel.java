/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@NonnullByDefault
public class PermissionMessageChannel implements MessageChannel {

    private final String permission;

    public PermissionMessageChannel(String permission) {
        this.permission = permission;
    }

    public Collection<MessageReceiver> getMembers() {
        List<MessageReceiver> lmr = Sponge.getServer().getOnlinePlayers()
                .stream().filter(x -> x.hasPermission(this.permission)).collect(Collectors.toList());
        lmr.add(Sponge.getServer().getConsole());
        return lmr;
    }
}
