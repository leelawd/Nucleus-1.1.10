/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.mixins.config.NucleusMixinConfig;
import io.github.nucleuspowered.nucleus.mixins.interfaces.INucleusMixinChunkProviderServer;

public class MixinConfigProxy {

    private Boolean isWorldGen;

    public boolean testWorldGen() {
        if (isWorldGen == null) {
            if (get().config.isInvsee()) {
                try {
                    if (get().config.isInvsee()) {
                        INucleusMixinChunkProviderServer.class.getDeclaredMethod("loadForce", int.class, int.class);
                        this.isWorldGen = true;
                    } else {
                        this.isWorldGen = false;
                    }
                } catch (NoSuchMethodException e) {
                    this.isWorldGen = false;
                }

                if (!this.isWorldGen) {
                    Nucleus.getNucleus().getLogger().warn("-----------------------------------------------------------------------");
                    Nucleus.getNucleus().getLogger().warn("You are using an OUTDATED version of Nucleus Mixins. 0.25+ is required.");
                    Nucleus.getNucleus().getLogger()
                            .warn("Please update to Mixins version 0.25.0: https://ore.spongepowered.org/Nucleus/Nucleus-Mixins");
                    Nucleus.getNucleus().getLogger().warn("-----------------------------------------------------------------------");
                }
            } else {
                this.isWorldGen = false;
            }
        }

        return isWorldGen;
    }

    public NucleusMixinConfig get() {
        return NucleusMixinConfig.INSTANCE;
    }
}
