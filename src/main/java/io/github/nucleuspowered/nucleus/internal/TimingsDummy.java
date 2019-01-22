/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import co.aikar.timings.Timing;

public class TimingsDummy implements Timing {

    public final static Timing DUMMY = new TimingsDummy();

    private TimingsDummy() {}

    @Override public Timing startTiming() {
        return this;
    }

    @Override public void stopTiming() {

    }

    @Override public void startTimingIfSync() {

    }

    @Override public void stopTimingIfSync() {

    }

    @Override public void abort() {

    }

    @Override public void close() {

    }
}
