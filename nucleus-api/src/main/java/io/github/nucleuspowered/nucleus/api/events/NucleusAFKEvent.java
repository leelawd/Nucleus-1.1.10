/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.events;

import io.github.nucleuspowered.nucleus.api.annotations.MightOccurAsync;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;

public interface NucleusAFKEvent extends TargetPlayerEvent {

    /**
     * Fired when a player goes AFK.
     *
     * <p>
     *     <strong>This event might fire async!</strong>
     * </p>
     */
    @MightOccurAsync
    interface GoingAFK extends NucleusAFKEvent {}

    /**
     * Fired when a player returns from AFK.
     *
     * <p>
     *     <strong>This event might fire async!</strong>
     * </p>
     */
    @MightOccurAsync
    interface ReturningFromAFK extends NucleusAFKEvent {}

    /**
     * Fired when a player is about to be kicked due to inactivity.
     *
     * <p>
     *     If this event is cancelled, the player will not be kicked for inactivity until the player comes back from AFK and goes AFK again.
     * </p>
     * <p>
     *     <strong>This event might fire async!</strong>
     * </p>
     */
    @MightOccurAsync
    interface Kick extends NucleusAFKEvent, Cancellable {}
}
