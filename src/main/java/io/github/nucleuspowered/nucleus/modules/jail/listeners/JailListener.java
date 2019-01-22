/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.listeners;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.events.NucleusSendToSpawnEvent;
import io.github.nucleuspowered.nucleus.api.events.NucleusTeleportEvent;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.core.events.NucleusOnLoginEvent;
import io.github.nucleuspowered.nucleus.modules.fly.datamodules.FlyUserDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.commands.JailCommand;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailUserDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class JailListener extends ListenerBase.Reloadable {

    @Inject private UserDataManager loader;
    @Inject private InternalServiceManager ism;
    @Inject private JailConfigAdapter jailConfigAdapter;
    @Inject private JailHandler handler;
    private final String notify;
    private final String teleport;
    private final String teleportto;

    private boolean isMuteOnJail = false;
    private List<String> allowedCommands;

    @Inject
    public JailListener(NucleusPlugin plugin) {
        notify = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(JailCommand.class).getPermissionWithSuffix("notify");
        teleport = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(JailCommand.class).getPermissionWithSuffix("teleportjailed");
        teleportto = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(JailCommand.class).getPermissionWithSuffix("teleporttojailed");
    }

    @Listener
    public void onPlayerLogin(final NucleusOnLoginEvent event, @Getter("getTargetUser") User user, @Getter("getUserService") ModularUserService qs) {
        JailHandler handler = ism.getService(JailHandler.class).get();
        JailUserDataModule userDataModule = qs.get(JailUserDataModule.class);

        // Jailing the subject if we need to.
        if (userDataModule.jailOnNextLogin() && userDataModule.getJailData().isPresent()) {
            Optional<NamedLocation> owl = handler.getWarpLocation(user);
            if (!owl.isPresent()) {
                new PermissionMessageChannel(notify)
                    .send(Text.of(TextColors.RED, "WARNING: No jail is defined. Jailed players are going free!"));
                handler.unjailPlayer(user);
                return;
            }

            JailData jd = userDataModule.getJailData().get();
            jd.setPreviousLocation(event.getFrom().getLocation());
            userDataModule.setJailData(jd);
            event.setTo(owl.get().getTransform().get());
        }
    }

    /**
     * At the time the subject joins, check to see if the subject is muted.
     *
     * @param event The event.
     */
    @Listener(order = Order.LATE)
    public void onPlayerJoin(final ClientConnectionEvent.Join event) {
        final Player user = event.getTargetEntity();
        Optional<ModularUserService> oqs = loader.get(user);
        if (!oqs.isPresent()) {
            return;
        }

        JailUserDataModule qs = oqs.get().get(JailUserDataModule.class);
        JailHandler handler = ism.getService(JailHandler.class).get();

        // Jailing the subject if we need to.
        Optional<JailData> data = handler.getPlayerJailDataInternal(user);
        if (qs.jailOnNextLogin() && data.isPresent()) {
            // It exists.
            NamedLocation owl = handler.getWarpLocation(user).get();
            JailData jd = data.get();
            Optional<Duration> timeLeft = jd.getRemainingTime();
            Text message;
            message = timeLeft.map(duration -> plugin.getMessageProvider()
                .getTextMessageWithFormat("command.jail.jailed", owl.getName(), plugin.getNameUtil().getNameFromUUID(jd.getJailerInternal()),
                    plugin.getMessageProvider().getMessageWithFormat("standard.for"), Util.getTimeStringFromSeconds(duration.getSeconds())))
                .orElseGet(() -> plugin.getMessageProvider()
                    .getTextMessageWithFormat("command.jail.jailed", owl.getName(), plugin.getNameUtil().getNameFromUUID(jd.getJailerInternal()), "",
                        ""));

            oqs.get().get(FlyUserDataModule.class).setFlying(false);
            user.sendMessage(message);
            user.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("standard.reason", jd.getReason()));
        }

        qs.setJailOnNextLogin(false);

        // Kick off a scheduled task.
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS).execute(() -> {
            Optional<JailData> omd = qs.getJailData();
            if (omd.isPresent()) {
                JailData md = omd.get();
                md.nextLoginToTimestamp();

                omd = Util.testForEndTimestamp(qs.getJailData(), () -> handler.unjailPlayer(user));
                if (omd.isPresent()) {
                    md = omd.get();
                    handler.onJail(md, event.getTargetEntity());
                }
            }
        }).submit(plugin);
    }

    @Listener
    public void onRequestSent(NucleusTeleportEvent.Request event, @Root Player cause, @Getter("getTargetEntity") Player player) {
        if (handler.isPlayerJailed(cause)) {
            event.setCancelled(true);
            event.setCancelMessage(plugin.getMessageProvider().getTextMessageWithFormat("jail.teleportcause.isjailed"));
        } else if (handler.isPlayerJailed(player)) {
            event.setCancelled(true);
            event.setCancelMessage(plugin.getMessageProvider().getTextMessageWithFormat("jail.teleporttarget.isjailed", player.getName()));
        }
    }

    @Listener
    public void onAboutToTeleport(NucleusTeleportEvent.AboutToTeleport event, @Root CommandSource cause, @Getter("getTargetEntity") Player player) {
        if (handler.isPlayerJailed(player)) {
            if (!cause.hasPermission(teleport)) {
                event.setCancelled(true);
                event.setCancelMessage(plugin.getMessageProvider().getTextMessageWithFormat("jail.abouttoteleporttarget.isjailed", player.getName()));
            } else if (!player.hasPermission(teleportto)) {
                event.setCancelled(true);
                event.setCancelMessage(plugin.getMessageProvider().getTextMessageWithFormat("jail.abouttoteleportcause.targetisjailed",
                        player.getName()));
            }
        }
    }

    @Listener
    public void onCommand(SendCommandEvent event, @Root Player player) {
        // Only if the command is not in the control list.
        if (handler.checkJail(player, false) && allowedCommands.stream().noneMatch(x -> event.getCommand().equalsIgnoreCase(x))) {
            event.setCancelled(true);

            // This is the easiest way to send the messages.
            handler.checkJail(player, true);
        }
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        event.setCancelled(handler.checkJail(player, true));
    }

    @Listener
    public void onInteract(InteractEvent event, @Root Player player) {
        event.setCancelled(handler.checkJail(player, true));
    }

    @Listener
    public void onSpawn(RespawnPlayerEvent event) {
        if (handler.checkJail(event.getTargetEntity(), false)) {
            event.setToTransform(event.getToTransform().setLocation(handler.getWarpLocation(event.getTargetEntity()).get().getLocation().get()));
        }
    }

    @Listener
    public void onSendToSpawn(NucleusSendToSpawnEvent event, @Getter("getTargetUser") User user) {
        if (handler.checkJail(user, false)) {
            event.setCancelled(true);
            event.setCancelReason(plugin.getMessageProvider().getMessageWithFormat("jail.isjailed"));
        }
    }

    @Override public void onReload() throws Exception {
        isMuteOnJail = jailConfigAdapter.getNodeOrDefault().isMuteOnJail();
        allowedCommands = jailConfigAdapter.getNodeOrDefault().getAllowedCommands();
    }
}
