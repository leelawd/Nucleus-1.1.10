/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit;

import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import javax.inject.Inject;

@ModuleData(id = KitModule.ID, name = "Kit")
public class KitModule extends ConfigurableModule<KitConfigAdapter> {

    public static final String ID = "kit";

    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            KitHandler kitHandler = new KitHandler();
            plugin.registerReloadable(kitHandler::reload);
            plugin.getInjector().injectMembers(kitHandler);
            serviceManager.registerService(KitHandler.class, kitHandler);
            game.getServiceManager().setProvider(plugin, NucleusKitService.class, kitHandler);
        } catch (Exception ex) {
            logger.warn("Could not load the kits module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public KitConfigAdapter createAdapter() {
        return new KitConfigAdapter();
    }
}
