/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk;

import io.github.nucleuspowered.nucleus.api.service.NucleusAFKService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = AFKModule.ID, name = "AFK")
public class AFKModule extends ConfigurableModule<AFKConfigAdapter> {

    public static final String ID = "afk";

    @Override
    public AFKConfigAdapter createAdapter() {
        return new AFKConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        AFKHandler handler = new AFKHandler(plugin, getAdapter());
        serviceManager.registerService(AFKHandler.class, handler);
        Sponge.getServiceManager().setProvider(plugin, NucleusAFKService.class, handler);
    }
}
