/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.mute.commands.CheckMuteCommand;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import javax.inject.Inject;

@ModuleData(id = MuteModule.ID, name = "Mute")
public class MuteModule extends ConfigurableModule<MuteConfigAdapter> {

    public static final String ID = "mute";
    @Inject private Game game;
    @Inject private Logger logger;

    @Override
    public MuteConfigAdapter createAdapter() {
        return new MuteConfigAdapter();
    }

    @Override
    protected void performPreTasks() throws Exception {
        super.performPreTasks();

        try {
            MuteHandler m = new MuteHandler();
            serviceManager.registerService(MuteHandler.class, m);
            Sponge.getServiceManager().provide(PermissionService.class).ifPresent(x -> x.registerContextCalculator(m));
        } catch (Exception ex) {
            logger.warn("Could not load the mute module for the reason below.");
            ex.printStackTrace();
            throw ex;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        createSeenModule(CheckMuteCommand.class, (c, u) -> {

            // If we have a ban service, then check for a ban.
            MuteHandler jh = plugin.getInternalServiceManager().getService(MuteHandler.class).get();
            if (jh.isMuted(u)) {
                MuteData jd = jh.getPlayerMuteData(u).get();
                // Lightweight checkban.
                Text.Builder m;
                if (jd.getRemainingTime().isPresent()) {
                    m = plugin.getMessageProvider().getTextMessageWithFormat("seen.ismuted.temp", Util.getTimeToNow(jd.getEndTimestamp().get())).toBuilder();
                } else {
                    m = plugin.getMessageProvider().getTextMessageWithFormat("seen.ismuted.perm").toBuilder();
                }

                return Lists.newArrayList(
                        m.onClick(TextActions.runCommand("/checkmute " + u.getName()))
                                .onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("standard.clicktoseemore"))).build(),
                        plugin.getMessageProvider().getTextMessageWithFormat("standard.reason", jd.getReason()));
            }

            return Lists.newArrayList(plugin.getMessageProvider().getTextMessageWithFormat("seen.notmuted"));
        });
    }
}
