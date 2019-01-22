/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.datamodules;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;

import java.time.Instant;
import java.util.Map;

public class KitUserDataModule extends DataModule<ModularUserService> {

    @DataKey("kitLastUsedTime")
    private Map<String, Long> kitLastUsedTime = Maps.newHashMap();

    public Map<String, Instant> getKitLastUsedTime() {
        final Map<String, Instant> r = Maps.newHashMap();
        kitLastUsedTime.forEach((k, v) -> r.put(k.toLowerCase(), Instant.ofEpochSecond(v)));
        return r;
    }

    public void addKitLastUsedTime(String kitName, Instant lastTime) {
        kitLastUsedTime.put(kitName.toLowerCase(), lastTime.getEpochSecond());
    }

    public void removeKitLastUsedTime(String kitName) {
        kitLastUsedTime.remove(kitName.toLowerCase());
    }
}
