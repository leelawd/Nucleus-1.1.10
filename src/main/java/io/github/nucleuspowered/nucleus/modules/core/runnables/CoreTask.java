/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.runnables;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

/**
 * Core tasks. No module, must always run.
 */
public class CoreTask extends TaskBase {
    @Inject private NucleusPlugin plugin;
    @Inject private CoreConfigAdapter cca;
    @Inject private UserDataManager uda;

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(5, ChronoUnit.MINUTES);
    }

    @Override
    public void accept(Task task) {
        if (cca.getNodeOrDefault().isDebugmode()) {
            plugin.getLogger().info(plugin.getMessageProvider().getMessageWithFormat("core.savetask.starting"));
        }

        plugin.saveData();
        uda.removeOfflinePlayers();

        if (cca.getNodeOrDefault().isDebugmode()) {
            plugin.getLogger().info(plugin.getMessageProvider().getMessageWithFormat("core.savetask.complete"));
        }
    }
}
