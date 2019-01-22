/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core;

import io.github.nucleuspowered.nucleus.api.service.NucleusPlayerMetadataService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.service.PlayerMetadataService;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = CoreModule.ID, name = "Core", isRequired = true)
public class CoreModule extends ConfigurableModule<CoreConfigAdapter> {

    public static final String ID = "core";

    @Override
    public CoreConfigAdapter createAdapter() {
        return new CoreConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        plugin.reloadMessages();
    }

    @Override public void onEnable() {
        super.onEnable();

        // Register service
        Sponge.getServiceManager().setProvider(this.plugin, NucleusPlayerMetadataService.class, new PlayerMetadataService());
    }
}
