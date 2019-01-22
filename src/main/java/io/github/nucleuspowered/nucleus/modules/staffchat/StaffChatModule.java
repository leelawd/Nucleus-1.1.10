/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat;

import io.github.nucleuspowered.nucleus.api.service.NucleusStaffChatService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.staffchat.config.StaffChatConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.staffchat.service.StaffChatService;
import org.spongepowered.api.Sponge;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@ModuleData(id = StaffChatModule.ID, name = "Staff Chat")
public class StaffChatModule extends ConfigurableModule<StaffChatConfigAdapter> {

    public static final String ID = "staff-chat";

    @Override
    public StaffChatConfigAdapter createAdapter() {
        return new StaffChatConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        // Registers itself.
        new StaffChatMessageChannel(plugin);
        Sponge.getServiceManager().setProvider(plugin, NucleusStaffChatService.class, new StaffChatService());
    }
}
