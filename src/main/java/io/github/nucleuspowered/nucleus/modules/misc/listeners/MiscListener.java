/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.listeners;

import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.misc.datamodules.InvulnerabilityUserDataModule;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.Getter;

import javax.inject.Inject;

public class MiscListener extends ListenerBase {

    @Inject private UserDataManager ucl;
    @Inject private CoreConfigAdapter cca;

    // For /god
    @Listener
    public void onPlayerStruck(DamageEntityEvent event, @Getter("getTargetEntity") Player pl) {
        if (ucl.getUser(pl).map(x -> x.get(InvulnerabilityUserDataModule.class).isInvulnerable()).orElse(false)) {
            pl.offer(Keys.FIRE_TICKS, 0);
            event.setBaseDamage(0);
            event.setCancelled(true);
        }
    }
}
