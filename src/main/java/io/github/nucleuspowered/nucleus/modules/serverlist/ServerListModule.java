/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.serverlist;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.serverlist.config.ServerListConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = ServerListModule.ID, name = "Server List")
public class ServerListModule extends ConfigurableModule<ServerListConfigAdapter> {

    public static final String ID = "server-list";

    @Override public ServerListConfigAdapter createAdapter() {
        return new ServerListConfigAdapter();
    }
}
