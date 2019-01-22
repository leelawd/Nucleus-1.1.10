/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.geoip.listeners;

import com.maxmind.geoip2.record.Country;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.modules.geoip.GeoIpModule;
import io.github.nucleuspowered.nucleus.modules.geoip.commands.GeoIpCommand;
import io.github.nucleuspowered.nucleus.modules.geoip.config.GeoIpConfig;
import io.github.nucleuspowered.nucleus.modules.geoip.config.GeoIpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.geoip.handlers.GeoIpDatabaseHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.util.Optional;
import java.util.function.Predicate;

import javax.inject.Inject;

@ConditionalListener(GeoIpListener.Condition.class)
public class GeoIpListener extends ListenerBase {

    private CommandPermissionHandler commandPermissionHandler = null;

    @Inject
    private GeoIpDatabaseHandler handler;

    @Listener(order = Order.LAST)
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        if (commandPermissionHandler == null) {
            commandPermissionHandler = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(GeoIpCommand.class);
        }

        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            try {
                Optional<Country> result = handler.getDetails(event.getTargetEntity().getConnection().getAddress().getAddress()).get();
                if (result.isPresent()) {
                    new PermissionMessageChannel(commandPermissionHandler.getPermissionWithSuffix("login"))
                        .send(plugin.getMessageProvider().getTextMessageWithFormat("geoip.playerfrom", event.getTargetEntity().getName(), result.get().getName()));
                } else {
                    new PermissionMessageChannel(commandPermissionHandler.getPermissionWithSuffix("login"))
                        .send(plugin.getMessageProvider().getTextMessageWithFormat("geoip.noinfo", event.getTargetEntity().getName()));
                }
            } catch (Exception e) {
                if (plugin.isDebugMode()) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static final class Condition implements Predicate<Nucleus> {

        private static boolean isPrinted = false;

        @Override public boolean test(Nucleus nucleus) {
            try {
                GeoIpConfig gic = nucleus.getModuleContainer().getConfigAdapterForModule(GeoIpModule.ID, GeoIpConfigAdapter.class).getNodeOrDefault();
                if (gic.isAcceptLicence()) {
                    if (!isPrinted) {
                        nucleus.getLogger()
                            .info("GeoIP is enabled. Nucleus makes use of GeoLite2 data created by MaxMind, available from http://www.maxmind.com");
                        isPrinted = true;
                    }

                    return gic.isAlertOnLogin();
                }

                return false;
            } catch (NoModuleException | IncorrectAdapterTypeException e) {
                if (nucleus.isDebugMode()) {
                    e.printStackTrace();
                }

                return false;
            }
        }
    }
}
