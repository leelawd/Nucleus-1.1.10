/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.misc.runnables;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.misc.datamodules.InvulnerabilityUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.entity.FoodData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Temporary solution until we can check for feeding levels to drop.
 */
@NonnullByDefault
public class GodRunnable extends TaskBase {

    private final NucleusPlugin plugin;
    private final UserDataManager ucl;

    private int defaultFoodLevel = 0;
    private double defaultSaturationLevel = 0;
    private double defaultExhaustionLevel = 0;

    @Inject
    public GodRunnable(NucleusPlugin plugin, UserDataManager ucl) {
        this.plugin = plugin;
        this.ucl = ucl;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public Duration interval() {
        return Duration.of(5, ChronoUnit.SECONDS);
    }

    @Override
    public void accept(Task task) {
        Collection<Player> cp = Sponge.getServer().getOnlinePlayers();
        if (cp.isEmpty()) {
            return;
        }

        if (this.defaultFoodLevel == 0) {
            FoodData def = cp.iterator().next().getFoodData();
            this.defaultFoodLevel = def.foodLevel().getDefault();
            this.defaultSaturationLevel = def.saturation().getDefault();
            this.defaultExhaustionLevel = def.exhaustion().getDefault();
        }

        List<Player> toFeed = cp.stream().filter(x -> ucl.getUser(x)
                .map(y -> y.get(InvulnerabilityUserDataModule.class).isInvulnerable()).orElse(false))
                .collect(Collectors.toList());
        if (!toFeed.isEmpty()) {
            Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> toFeed.forEach(p -> {
                p.offer(Keys.FOOD_LEVEL, this.defaultFoodLevel);
                p.offer(Keys.EXHAUSTION, this.defaultExhaustionLevel);
                p.offer(Keys.SATURATION, this.defaultSaturationLevel);
            }));
        }
    }
}
