/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.connection;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.connection.config.ConnectionConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = ConnectionModule.ID, name = "Connection")
public class ConnectionModule extends ConfigurableModule<ConnectionConfigAdapter> {

    public static final String ID = "connection";

    @Override
    public ConnectionConfigAdapter createAdapter() {
        return new ConnectionConfigAdapter();
    }
}
