/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.inventory;

import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.inventory.config.InventoryConfigAdapter;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = InventoryModule.ID, name = "Inventory")
public class InventoryModule extends ConfigurableModule<InventoryConfigAdapter> {

    public static final String ID = "inventory";

    @Override public InventoryConfigAdapter createAdapter() {
        return new InventoryConfigAdapter();
    }
}
