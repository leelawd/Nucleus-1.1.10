/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.freezeplayer;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.StandardModule;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.commands.FreezePlayerCommand;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.datamodules.FreezePlayerUserDataModule;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.Optional;

@ModuleData(id = "freeze-subject", name = "Freeze Player")
public class FreezePlayerModule extends StandardModule {

    @Override public void onEnable() {
        super.onEnable();

        createSeenModule(FreezePlayerCommand.class, (c, u) -> {
            Optional<ModularUserService> us = plugin.getUserDataManager().get(u);
            if (us.isPresent() && us.get().get(FreezePlayerUserDataModule.class).isFrozen()) {
                return Lists.newArrayList(plugin.getMessageProvider().getTextMessageWithFormat("seen.frozen"));
            }

            return Lists.newArrayList(plugin.getMessageProvider().getTextMessageWithFormat("seen.notfrozen"));
        });
    }
}
