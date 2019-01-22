/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.deathmessage;

import static io.github.nucleuspowered.nucleus.modules.deathmessage.DeathMessageModule.ID;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.deathmessage.config.DeathMessageConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = ID, name = "Death Messages")
public class DeathMessageModule extends ConfigurableModule<DeathMessageConfigAdapter> {

    public static final String ID = "death-message";

    @Override public DeathMessageConfigAdapter createAdapter() {
        return new DeathMessageConfigAdapter();
    }
}
