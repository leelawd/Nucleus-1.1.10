/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.LoggerWrapper;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.modules.world.commands.border.gen.EnhancedGeneration;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Inject;

public class WorldHelper {

    @Inject private NucleusPlugin plugin;

    private final Map<UUID, Task> pregen = Maps.newHashMap();

    public boolean isPregenRunningForWorld(UUID uuid) {
        cleanup();
        return pregen.containsKey(uuid);
    }

    public boolean addPregenForWorld(World world, EnhancedGeneration.NucleusChunkPreGenerator preGenerator, Boolean aggressive) {
        cleanup();
        if (!isPregenRunningForWorld(world.getUniqueId())) {
            if (aggressive) {
                pregen.put(world.getUniqueId(), Sponge.getScheduler().createTaskBuilder().intervalTicks(3).execute(preGenerator).submit(plugin));
            } else {
                pregen.put(world.getUniqueId(), Sponge.getScheduler().createTaskBuilder().intervalTicks(10).execute(preGenerator).submit(plugin));
            }

            return true;
        }

        return false;
    }

    public boolean startPregenningForWorld(World world, boolean agressive, @Nullable Integer tickPercent) {
        cleanup();
        if (!isPregenRunningForWorld(world.getUniqueId())) {
            WorldProperties wp = world.getProperties();
            WorldBorder.ChunkPreGenerate wbcp = world.newChunkPreGenerate(wp.getWorldBorderCenter(), wp.getWorldBorderDiameter())
                .owner(plugin).logger(new GenerationLogger(plugin.getLogger(), world.getName()));
            if (agressive) {
                wbcp.tickPercentLimit(0.9f).tickInterval(3);
            }

            if (tickPercent != null) {
                wbcp.tickPercentLimit(Math.max(0f, Math.min(tickPercent / 100.0f, 1f)));
            }

            pregen.put(world.getUniqueId(), wbcp.start());
            return true;
        }

        return false;
    }

    public boolean cancelPregenRunningForWorld(UUID uuid) {
        cleanup();
        if (pregen.containsKey(uuid)) {
            pregen.remove(uuid).cancel();
            return true;
        }

        return false;
    }

    private synchronized void cleanup() {
        pregen.entrySet().removeIf(x -> !Sponge.getScheduler().getTaskById(x.getValue().getUniqueId()).isPresent());
    }

    /**
     * This logger allows us to log some of the generation messages, but removes most of the spam.
     * We use it to just gently remind the user what's going on every couple of seconds.
     */
    private static class GenerationLogger extends LoggerWrapper {
        private final String worldName;
        private int count = 0;

        private boolean sendLog() {
            if (++count % 20 == 0) {
                count = 0;
                wrappedLogger.info(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("command.world.gen.continue", worldName));
                return true;
            }

            return false;
        }

        private GenerationLogger(Logger wrappedLogger, String worldName) {
            super(wrappedLogger);
            this.worldName = worldName;
        }

        @Override
        public void info(String format, Object... arguments) {
            // Ugly, but hey...
            if (sendLog()) {
                wrappedLogger.info(format, arguments);
            }
        }
    }
}
