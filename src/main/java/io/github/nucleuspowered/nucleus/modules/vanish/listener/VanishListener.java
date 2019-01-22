/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.listener;

import io.github.nucleuspowered.nucleus.api.events.NucleusMessageEvent;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.vanish.commands.VanishCommand;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfig;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import javax.inject.Inject;

public class VanishListener extends ListenerBase {

    @Inject private UserDataManager userDataManager;
    @Inject private VanishConfigAdapter configAdapter;

    private CommandPermissionHandler commandPermissionHandler = null;

    private CommandPermissionHandler getVanishHandler() {
        if (commandPermissionHandler == null) {
            commandPermissionHandler = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(VanishCommand.class);
        }

        return commandPermissionHandler;
    }

    @Listener
    public void onLogin(ClientConnectionEvent.Join event, @Root Player player) {
        VanishUserDataModule service = userDataManager.get(player).get().get(VanishUserDataModule.class);
        if (service.isVanished()) {
            VanishConfig vanishConfig = configAdapter.getNodeOrDefault();
            if (!getVanishHandler().testSuffix(player, "persist")) {
                // No permission, no vanish.
                service.setVanished(false);
                return;
            } else if (vanishConfig.isSuppressMessagesOnVanish()) {
                event.setMessageCancelled(true);
            }

            player.offer(Keys.VANISH, true);
            player.offer(Keys.VANISH_IGNORES_COLLISION, true);
            player.offer(Keys.VANISH_PREVENTS_TARGETING, true);
            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("vanish.login"));
        }
    }

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {
        player.get(Keys.VANISH).ifPresent(x -> {
            if (x) {
                userDataManager.get(player).get().get(VanishUserDataModule.class).setVanished(true);
                if (configAdapter.getNodeOrDefault().isSuppressMessagesOnVanish()) {
                    event.setMessageCancelled(true);
                }
            }
        });
    }

    public void onNucleusMessage(NucleusMessageEvent event) {

    }
}
