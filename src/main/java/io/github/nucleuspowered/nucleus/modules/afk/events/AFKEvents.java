/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusAFKEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
public abstract class AFKEvents extends AbstractEvent implements TargetPlayerEvent {

    private final Player target;
    private final Cause cause;

    public AFKEvents(Player target) {
        this(target, Cause.of(NamedCause.owner(target)));
    }

    public AFKEvents(Player target, Cause cause) {
        this.target = target;
        this.cause = cause;
    }

    @Override public Player getTargetEntity() {
        return this.target;
    }

    @Override public Cause getCause() {
        return this.cause;
    }

    public static class From extends AFKEvents implements NucleusAFKEvent.ReturningFromAFK {

        public From(Player target) {
            super(target);
        }

        public From(Player target, Cause cause) {
            super(target, cause);
        }
    }

    public static class To extends AFKEvents implements NucleusAFKEvent.GoingAFK {

        public To(Player target) {
            super(target);
        }

        public To(Player target, Cause cause) {
            super(target, cause);
        }
    }

    public static class Kick extends AFKEvents implements NucleusAFKEvent.Kick {

        private boolean cancelled = false;

        public Kick(Player target) {
            super(target);
        }

        public Kick(Player target, Cause cause) {
            super(target, cause);
        }

        @Override public boolean isCancelled() {
            return cancelled;
        }

        @Override public void setCancelled(boolean cancel) {
            this.cancelled = cancel;
        }
    }
}
