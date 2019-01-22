/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.environment.listeners;

import io.github.nucleuspowered.nucleus.dataservices.loaders.WorldDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.environment.datamodule.EnvironmentWorldDataModule;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;

import javax.inject.Inject;

public class EnvironmentListener extends ListenerBase {

    @Inject private WorldDataManager loader;

    @Listener
    public void onWeatherChange(ChangeWorldWeatherEvent event) {
        loader.getWorld(event.getTargetWorld()).ifPresent(x ->
            event.setCancelled(x.quickGet(EnvironmentWorldDataModule.class, EnvironmentWorldDataModule::isLockWeather)));
    }
}
