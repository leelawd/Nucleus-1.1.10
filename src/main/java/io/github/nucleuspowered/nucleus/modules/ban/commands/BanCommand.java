/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.argumentparsers.GameProfileArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@RegisterCommand("ban")
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@EssentialsEquivalent("ban")
@NonnullByDefault
public class BanCommand extends AbstractCommand<CommandSource> {

    private final Logger logger;
    private final CoreConfigAdapter cca;

    static final String notifyPermission = PermissionRegistry.PERMISSIONS_PREFIX + "ban.notify";
    private final String uuid = "uuid";
    private final String user = "user";
    private final String name = "name";

    private final String reason = "reason";

    @Inject
    public BanCommand(Logger logger, CoreConfigAdapter cca) {
        this.logger = logger;
        this.cca = cca;
    }

    @Override
    public Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> ps = Maps.newHashMap();
        ps.put(notifyPermission, PermissionInformation.getWithTranslation("permission.ban.notify", SuggestedLevel.MOD));
        return ps;
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("offline", PermissionInformation.getWithTranslation("permission.ban.offline", SuggestedLevel.MOD));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.tempban.exempt.target", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.firstParsing(
                        GenericArguments.onlyOne(UUIDArgument.gameProfile(Text.of(uuid))),
                        GenericArguments.onlyOne(new GameProfileArgument(Text.of(user))),
                        GenericArguments.onlyOne(GenericArguments.string(Text.of(name)))
                ),
                GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of(reason)))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        final String r = args.<String>getOne(reason).orElse(plugin.getMessageProvider().getMessageWithFormat("ban.defaultreason"));
        Optional<GameProfile> ou = Optional.ofNullable(args.<GameProfile>getOne(uuid).orElseGet(() -> args.<GameProfile>getOne(user).orElse(null)));
        if (ou.isPresent()) {
            Optional<User> optionalUser = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(ou.get());
            if ((!optionalUser.isPresent() || !optionalUser.get().isOnline()) && !permissions.testSuffix(src, "offline")) {
                throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.offline.noperms"));
            }

            if (optionalUser.isPresent() && permissions.testSuffix(optionalUser.get(), "exempt.target", src, false)) {
                throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.exempt", optionalUser.get().getName()));
            }

            return executeBan(src, ou.get(), r);
        }

        if (!permissions.testSuffix(src, "offline")) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.offline.noperms"));
        }

        final String userToFind = args.<String>getOne(name).get();

        // Get the profile async.
        Sponge.getScheduler().createAsyncExecutor(plugin).execute(() -> {
            GameProfileManager gpm = Sponge.getServer().getGameProfileManager();
            try {
                GameProfile gp = gpm.get(userToFind).get();

                // Ban the user sync.
                Sponge.getScheduler().createSyncExecutor(plugin).execute(() -> {
                    // Create the user.
                    UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                    User user = uss.getOrCreate(gp);
                    src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("gameprofile.new", user.getName()));

                    try {
                        executeBan(src, gp, r);
                    } catch (Exception e) {
                        if (cca.getNodeOrDefault().isDebugmode()) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                if (cca.getNodeOrDefault().isDebugmode()) {
                    e.printStackTrace();
                }

                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.profileerror", userToFind));
            }
        });

        return CommandResult.empty();
    }

    private CommandResult executeBan(CommandSource src, GameProfile u, String r) {
        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        User user = uss.get(u).get();
        if (!user.isOnline() && !permissions.testSuffix(src, "offline")) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.offline.noperms"));
            return CommandResult.empty();
        }

        if (service.isBanned(u)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.alreadyset", u.getName().orElse(plugin.getMessageProvider().getMessageWithFormat("standard.unknown"))));
            return CommandResult.empty();
        }

        // Create the ban.
        Ban bp = Ban.builder().type(BanTypes.PROFILE).profile(u).source(src).reason(TextSerializers.FORMATTING_CODE.deserialize(r)).build();
        service.addBan(bp);

        // Get the permission, "quickstart.ban.notify"
        MutableMessageChannel send = new PermissionMessageChannel(notifyPermission).asMutable();
        send.addMember(src);
        send.send(plugin.getMessageProvider().getTextMessageWithFormat("command.ban.applied", u.getName().orElse(plugin.getMessageProvider().getMessageWithFormat("standard.unknown")), src.getName()));
        send.send(plugin.getMessageProvider().getTextMessageWithFormat("standard.reason", r));

        if (Sponge.getServer().getPlayer(u.getUniqueId()).isPresent()) {
            Sponge.getServer().getPlayer(u.getUniqueId()).get().kick(TextSerializers.FORMATTING_CODE.deserialize(r));
        }

        return CommandResult.success();
    }
}
