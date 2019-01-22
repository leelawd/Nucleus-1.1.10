/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;


import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.jail.JailModule;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfig;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailUserDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

@ConditionalListener(LogoutJailListener.Condition.class)
public class LogoutJailListener extends ListenerBase {

    @Inject private UserDataManager loader;
    @Inject private JailHandler handler;

    @Listener
    public void onLogout(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        loader.get(player).ifPresent(mus -> {
            Optional<JailData> ojd = mus.get(JailUserDataModule.class).getJailData();
            if (ojd.isPresent()) {
                JailData jd = ojd.get();
                Optional<Instant> end = jd.getEndTimestamp();
                end.ifPresent(instant -> jd.setTimeFromNextLogin(Duration.between(Instant.now(), instant)));
                mus.get(JailUserDataModule.class).setJailData(jd);
            }
        });
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            return Nucleus.getNucleus().getConfigValue(JailModule.ID, JailConfigAdapter.class, JailConfig::isJailOnlineOnly).orElseGet(() -> false);
        }
    }
}
