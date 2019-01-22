/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop;

import io.github.nucleuspowered.nucleus.api.service.NucleusServerShopService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.servershop.config.ServerShopConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.servershop.services.ItemWorthService;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = ServerShopModule.ID, name = "Server Shop")
public class ServerShopModule extends ConfigurableModule<ServerShopConfigAdapter> {

    public static final String ID = "server-shop";

    @Override
    public ServerShopConfigAdapter createAdapter() {
        return new ServerShopConfigAdapter();
    }

    @Override public void onEnable() {
        super.onEnable();

        ItemWorthService service = new ItemWorthService(plugin);
        serviceManager.registerService(ItemWorthService.class, service);
        Sponge.getServiceManager().setProvider(plugin, NucleusServerShopService.class, service);
    }
}
