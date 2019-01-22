/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.CancellableTask;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

public abstract class CostCancellableTask implements CancellableTask {

    protected final double cost;
    protected final Nucleus plugin;
    protected final SubjectPermissionCache<? extends CommandSource> subject;
    private boolean hasRun = false;

    public CostCancellableTask(Nucleus plugin, SubjectPermissionCache<? extends CommandSource> src, double cost) {
        this.plugin = plugin;
        this.subject = src;
        this.cost = cost;
    }

    @Override
    public void onCancel() {
        if (!hasRun) {
            hasRun = true;
            if (subject.getSubject() instanceof Player && cost > 0) {
                Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> plugin.getEconHelper().depositInPlayer((Player)subject.getSubject(), cost));
            }
        }
    }
}
