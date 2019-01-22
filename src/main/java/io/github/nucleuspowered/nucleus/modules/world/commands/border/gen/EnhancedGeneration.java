/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border.gen;

import com.flowpowered.math.GenericMath;
import com.flowpowered.math.vector.Vector3i;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.mixins.interfaces.INucleusMixinWorld;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import io.github.nucleuspowered.nucleus.modules.world.commands.border.GenerateChunksCommand;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfigAdapter;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import io.github.nucleuspowered.nucleus.util.TriFunction;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Generation for the NucleusMixin enhanced version.
 */
public class EnhancedGeneration implements TriFunction<World, CommandSource, CommandContext, CommandResult> {

    private final String notifyPermission;
    private final WorldHelper worldHelper;
    private final WorldConfigAdapter worldConfigAdapter;

    public EnhancedGeneration(WorldHelper worldHelper, String notifyPermission, WorldConfigAdapter worldConfigAdapter) {
        this.worldHelper = worldHelper;
        this.notifyPermission = notifyPermission;
        this.worldConfigAdapter = worldConfigAdapter;
    }

    @Override
    public CommandResult accept(World world, CommandSource source, CommandContext args) {
        boolean aggressive = args.hasAny("a");
        worldHelper.addPregenForWorld(
            world,
            new NucleusChunkPreGenerator(world, notifyPermission,
                    aggressive, args.<Long>getOne(Text.of(GenerateChunksCommand.saveTimeKey)).orElse(20L),
                    worldConfigAdapter,
                    args.<Integer>getOne(Text.of(GenerateChunksCommand.ticksKey)).orElse(null)),
            aggressive);

        source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gen.using.enhanced"));
        source.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gen.started", world.getProperties().getWorldName()));
        return CommandResult.success();
    }

    /* (non-Javadoc)
     * This is based on the SpongeChunkPreGenerator by SpongePowered, and is reused under the terms of the MIT licence,
     * with modification.
     */
    public static class NucleusChunkPreGenerator implements Consumer<Task> {

        private static final Vector3i[] OFFSETS = {
                Vector3i.UNIT_Z.negate().mul(2),
                Vector3i.UNIT_X.mul(2),
                Vector3i.UNIT_Z.mul(2),
                Vector3i.UNIT_X.negate().mul(2)
        };
        private static final String TIME_FORMAT = "s's 'S'ms'";
        private final World world;
        private final int chunkRadius;
        private final long tickTimeLimit;
        private final String notifyPermission;
        private final long timeToSave;
        private double finalTotal;
        private Vector3i currentPosition;
        private int currentGenCount;
        private int currentLayer;
        private int currentIndex;
        private int nextJump;
        private int totalCount;
        private long totalTime;

        private int totalChunksGenerated = 0;
        private long originalStartTime;
        private long lastLogTime;
        private long lastSaveTime;
        private long deltaTime = 0;
        private int chunksGenerated = 0;
        private int skipped = 0;
        private boolean highMemTriggered = false;

        private final boolean aggressive;
        private final WorldConfigAdapter worldConfigAdapter;

        private NucleusChunkPreGenerator(World world, String permission, boolean aggressive, long timeToSave, WorldConfigAdapter worldConfigAdapter,
                Integer integer) {
            this.notifyPermission = permission;
            this.aggressive = aggressive;
            this.world = world;
            this.chunkRadius = GenericMath.floor(world.getWorldBorder().getDiameter() / (2 * Sponge.getServer().getChunkLayout().getChunkSize().getX()));

            if (integer != null) {
                this.tickTimeLimit = Math.round(Sponge.getScheduler().getPreferredTickInterval() * Math.max(0f, Math.min(1f, integer / 100f)));
            } else if (aggressive) {
                this.tickTimeLimit = Math.round(Sponge.getScheduler().getPreferredTickInterval() * 0.9);
            } else {
                this.tickTimeLimit = Math.round(Sponge.getScheduler().getPreferredTickInterval() * 0.8);
            }

            final Optional<Vector3i> currentPosition = Sponge.getServer().getChunkLayout().toChunk(world.getWorldBorder().getCenter().toInt());
            if (currentPosition.isPresent()) {
                this.currentPosition = currentPosition.get();
            } else {
                throw new IllegalArgumentException("Center is not a valid chunk coordinate");
            }
            this.currentGenCount = 4;
            this.currentLayer = 0;
            this.currentIndex = 0;
            this.nextJump = 0;
            this.totalCount = 0;
            this.totalTime = 0;
            this.finalTotal = Math.pow(this.chunkRadius * 2 + 1, 2);
            this.timeToSave = timeToSave * 1000L;
            this.worldConfigAdapter = worldConfigAdapter;
        }

        @Override
        public void accept(Task task) {
            final long startTime = System.currentTimeMillis();
            if (originalStartTime == 0) {
                originalStartTime = startTime;
                lastLogTime = startTime;
                lastSaveTime = startTime;
            }

            if (aggressive) {
                long percent = getMemPercent();
                if (percent >= 90) {
                    if (!highMemTriggered) {
                        // Try to force a GC to save as much memory as possible.
                        System.gc();

                        // Save to ensure data isn't lost...
                        save();
                        world.getLoadedChunks().forEach(Chunk::unloadChunk);

                        // Try to force a GC again.
                        System.gc();

                        highMemTriggered = getMemPercent() >= 90;
                    }
                } else if (highMemTriggered) {
                    highMemTriggered = false;
                }
            } else {
                long percent = getMemPercent();
                if (percent >= 90) {
                    if (!highMemTriggered) {
                        save();
                        world.getLoadedChunks().forEach(Chunk::unloadChunk);

                        // Ask the system to try to garbage collect.
                        System.gc();

                        if (getMemPercent() >= 90) {
                            getChannel().send(NucleusPlugin.getNucleus().getMessageProvider()
                                    .getTextMessageWithFormat("command.pregen.gen.memory.high", String.valueOf(percent)));
                            highMemTriggered = true;
                        }
                    } else {
                        // Try again next tick.
                        return;
                    }
                } else if (highMemTriggered && percent <= 80) {
                    // Get the memory usage down to 80% to prevent too much ping pong.
                    highMemTriggered = false;
                    getChannel().send(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.memory.low"));
                }
            }

            INucleusMixinWorld inmw = ((INucleusMixinWorld)world);
            do {
                final Vector3i position = nextChunkPosition();
                Vector3i pos1 = position.sub(Vector3i.UNIT_X);
                Vector3i pos2 = position.sub(Vector3i.UNIT_Z);
                Vector3i pos3 = pos2.sub(Vector3i.UNIT_X);

                // Try not to load chunks that have already been loaded to minimise memory usage.
                if (!(inmw.hasChunkBeenGenerated(position) && inmw.hasChunkBeenGenerated(pos1)
                        && inmw.hasChunkBeenGenerated(pos2) && inmw.hasChunkBeenGenerated(pos3))) {
                    // Only mark as generated if we actually have to generate at least one.
                    chunksGenerated += this.currentGenCount;
                    if (this.world.loadChunk(position, true).map(Extent::isLoaded).orElse(false)) {
                        this.world.loadChunk(position.sub(Vector3i.UNIT_X), true);
                        this.world.loadChunk(position.sub(Vector3i.UNIT_Z), true);
                        this.world.loadChunk(position.sub(Vector3i.UNIT_X).sub(Vector3i.UNIT_Z), true);
                    } else {
                        ((INucleusMixinWorld)(this.world)).loadChunkForce(position);
                        ((INucleusMixinWorld)(this.world)).loadChunkForce(position.sub(Vector3i.UNIT_X));
                        ((INucleusMixinWorld)(this.world)).loadChunkForce(position.sub(Vector3i.UNIT_Z));
                        ((INucleusMixinWorld)(this.world)).loadChunkForce(position.sub(Vector3i.UNIT_X).sub(Vector3i.UNIT_Z));
                    }
                } else {
                    skipped += this.currentGenCount;
                }
            } while (hasNextChunkPosition() && checkTickTime(System.currentTimeMillis() - startTime));

            final long currentTimeMillis = System.currentTimeMillis();
            deltaTime += currentTimeMillis - startTime;

            if (lastLogTime + 5000 < currentTimeMillis) {
                this.totalCount += (chunksGenerated + skipped);
                this.totalChunksGenerated += chunksGenerated;
                final long logTime = System.currentTimeMillis() - lastLogTime;
                this.totalTime += deltaTime;

                Text message;
                if (skipped > 0) {
                    message = NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.notifyskipped",
                            String.valueOf(chunksGenerated),
                            String.valueOf(skipped),
                            DurationFormatUtils.formatDuration(deltaTime, TIME_FORMAT, false),
                            DurationFormatUtils.formatDuration(logTime, TIME_FORMAT, false),
                            String.valueOf(GenericMath.floor((this.totalCount * 100) / this.finalTotal)));
                } else {
                    message = NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.notify",
                            String.valueOf(chunksGenerated),
                            DurationFormatUtils.formatDuration(deltaTime, TIME_FORMAT, false),
                            DurationFormatUtils.formatDuration(logTime, TIME_FORMAT, false),
                            String.valueOf(GenericMath.floor((this.totalCount * 100) / this.finalTotal)));
                }

                getChannel().send(message);
                lastLogTime = System.currentTimeMillis();
                deltaTime = 0;
                skipped = 0;
                chunksGenerated = 0;
            }

            if (lastSaveTime + timeToSave < currentTimeMillis) {
                if (worldConfigAdapter.getNodeOrDefault().isDisplayWarningGeneration()) {
                    MessageChannel.TO_ALL.send(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.all"));
                }

                save();
            }

            if (!hasNextChunkPosition()) {
                this.totalCount += (chunksGenerated + skipped);
                this.totalChunksGenerated += chunksGenerated;
                save();
                getChannel().send(
                        NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.completed",
                                String.valueOf(this.totalChunksGenerated),
                                String.valueOf(this.totalCount - this.totalChunksGenerated),
                                DurationFormatUtils.formatDuration(this.totalTime, TIME_FORMAT, false),
                                DurationFormatUtils.formatDuration(currentTimeMillis - this.originalStartTime, TIME_FORMAT, false)
                        ));
                task.cancel();
            }
        }

        private boolean hasNextChunkPosition() {
            return this.currentLayer <= this.chunkRadius;
        }

        private MessageChannel getChannel() {
            return MessageChannel.combined(MessageChannel.TO_CONSOLE, new PermissionMessageChannel(this.notifyPermission));
        }

        private Vector3i nextChunkPosition() {
            final Vector3i nextPosition = this.currentPosition;
            final int currentLayerIndex;
            if (this.currentIndex >= this.nextJump) {
                // Reached end of layer, jump to the next so we can keep spiralling
                this.currentPosition = this.currentPosition.sub(Vector3i.UNIT_X).sub(Vector3i.UNIT_Z);
                this.currentLayer++;
                // Each the jump increment increases by 4 at each new layer
                this.nextJump += this.currentLayer * 4;
                currentLayerIndex = 1;
            } else {
                // Get the current index since the last jump
                currentLayerIndex = this.currentIndex - (this.nextJump - this.currentLayer * 4);
                // Move to next position in layer, by following a square
                this.currentPosition = this.currentPosition.add(OFFSETS[currentLayerIndex / this.currentLayer]);
            }
            // If we're at the corner it's 3, else 2 for an edge
            this.currentGenCount = currentLayerIndex % this.currentLayer == 0 ? 3 : 2;
            this.currentIndex++;
            return nextPosition;
        }

        private boolean checkTickTime(long tickTime) {
            return tickTime < this.tickTimeLimit;
        }

        private long getMemPercent() {
            // Check system memory
            long max = Runtime.getRuntime().maxMemory() / 1024 / 1024;
            long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
            long free = Runtime.getRuntime().freeMemory() / 1024 / 1024;

            return ((max - total + free ) * 100)/ max;
        }

        private void save() {
            getChannel().send(
                    NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.saving"));
            try {
                world.save();
                getChannel().send(
                        NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.saved"));
            } catch (Throwable e) {
                getChannel().send(
                        NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.pregen.gen.savefailed"));
                e.printStackTrace();
            } finally {
                lastSaveTime = System.currentTimeMillis();
            }
        }
    }
}
