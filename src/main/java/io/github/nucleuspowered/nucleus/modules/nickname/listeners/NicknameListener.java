/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.listeners;

import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import javax.inject.Inject;

public class NicknameListener extends ListenerBase {

    @Inject private UserDataManager ucl;

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player) {
        ucl.get(player).ifPresent(x -> {
            Optional<Text> d = x.get(NicknameUserDataModule.class).getNicknameAsText();
            if (d.isPresent()) {
                player.offer(Keys.DISPLAY_NAME, d.get());
            } else {
                player.remove(Keys.DISPLAY_NAME);
            }
        });
    }
}
