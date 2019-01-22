/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.events;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
import java.util.Optional;

/**
 * Events that are related about kits.
 */
@NonnullByDefault
public interface NucleusKitEvent extends Event {

    /**
     * Fired when a kit is redeemed.
     *
     * <ul>
     *     <li>
     *         <code>Pre</code> is fired before the kit is redeemed - extra checks should be performed.
     *     </li>
     *     <li>
     *         <code>Post</code> is fired after a kit is redeemed successfully (defined as a kit would not be redeemable again if it was a one time
     *         kit).
     *     </li>
     * </ul>
     */
    interface Redeem extends NucleusKitEvent, TargetPlayerEvent {

        /**
         * Gets the last time the kit was redeemed, if any.
         *
         * @return The {@link Instant} the kit was last redeemed.
         */
        Optional<Instant> getLastRedeemedTime();

        /**
         * Gets the name of the kit.
         *
         * @return The name of the kit.
         */
        String getName();

        /**
         * Gets the kit that has been redeemed.
         *
         * @return The kit that has been redeemed.
         */
        Kit getRedeemedKit();

        /**
         * Fired when a player has redeemed a kit.
         *
         * <p>
         *     This event will NOT fire when a player is given a kit and the checks have been bypassed.
         * </p>
         */
        interface Pre extends Redeem, CancelMessage {}

        /**
         * Fired when a player's kit has been updated.
         */
        interface Post extends Redeem {}

        /**
         * Fired when a player's kit could not be updated.
         */
        interface Failed extends Redeem {}
    }
}
