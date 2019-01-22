/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban;

import io.github.nucleuspowered.nucleus.api.service.NucleusNameBanService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.nameban.config.NameBanConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nameban.handlers.NameBanHandler;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = "nameban", name = "Name Banning")
public class NameBanModule extends ConfigurableModule<NameBanConfigAdapter> {

    @Override public NameBanConfigAdapter createAdapter() {
        return new NameBanConfigAdapter();
    }

    @Override protected void performPreTasks() throws Exception {
        super.performPreTasks();

        NameBanHandler handler = new NameBanHandler(plugin);
        plugin.getInternalServiceManager().registerService(NameBanHandler.class, handler);
        Sponge.getServiceManager().setProvider(plugin, NucleusNameBanService.class, handler);
    }
}
