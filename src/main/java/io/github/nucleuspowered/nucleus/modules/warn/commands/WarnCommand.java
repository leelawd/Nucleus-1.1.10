/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.commands;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warn.data.WarnData;
import io.github.nucleuspowered.nucleus.modules.warn.handlers.WarnHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"warn", "warning", "addwarning"})
public class WarnCommand extends AbstractCommand<CommandSource> {

    private final String playerKey = "subject";
    private final String durationKey = "duration";
    private final String reasonKey = "reason";

    private final WarnHandler warnHandler;
    private final WarnConfigAdapter wca;

    @Inject
    public WarnCommand(WarnHandler warnHandler, WarnConfigAdapter wca) {
        this.warnHandler = warnHandler;
        this.wca = wca;
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.length", PermissionInformation.getWithTranslation("permission.warn.exempt.length", SuggestedLevel.MOD));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.warn.exempt.target", SuggestedLevel.MOD));
        m.put("notify", PermissionInformation.getWithTranslation("permission.warn.notify", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{GenericArguments.onlyOne(GenericArguments.user(Text.of(playerKey))),
                GenericArguments.onlyOne(GenericArguments.optionalWeak(new TimespanArgument(Text.of(durationKey)))),
                GenericArguments.onlyOne(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reasonKey))))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(playerKey).get();
        Optional<Long> optDuration = args.getOne(durationKey);
        String reason = args.<String>getOne(reasonKey).get();

        if (permissions.testSuffix(user, "exempt.target", src, false)) {
            throw ReturnMessageException.fromKey("command.warn.exempt", user.getName());
        }

        //Set default duration if no duration given
        if (wca.getNodeOrDefault().getDefaultLength() != -1 && !optDuration.isPresent()) {
            optDuration = Optional.of(wca.getNodeOrDefault().getDefaultLength());
        }

        UUID warner = Util.getUUID(src);
        WarnData warnData = optDuration.map(aLong -> new WarnData(Instant.now(), warner, reason, Duration.ofSeconds(aLong)))
                .orElseGet(() -> new WarnData(Instant.now(), warner, reason));

        //Check if too long (No duration provided, it is infinite)
        if (!optDuration.isPresent() && wca.getNodeOrDefault().getMaximumWarnLength() != -1 && !permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.warn.length.toolong", Util.getTimeStringFromSeconds(wca.getNodeOrDefault().getMaximumWarnLength()));
        }

        //Check if too long
        if (optDuration.orElse(Long.MAX_VALUE) > wca.getNodeOrDefault().getMaximumWarnLength() && wca.getNodeOrDefault().getMaximumWarnLength() != -1 && !permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.warn.length.toolong", Util.getTimeStringFromSeconds(wca.getNodeOrDefault().getMaximumWarnLength()));
        }

        //Check if too short
        if (optDuration.orElse(Long.MAX_VALUE) < wca.getNodeOrDefault().getMinimumWarnLength() && wca.getNodeOrDefault().getMinimumWarnLength() != -1 && !permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.warn.length.tooshort", Util.getTimeStringFromSeconds(wca.getNodeOrDefault().getMinimumWarnLength()));
        }

        if (warnHandler.addWarning(user, warnData)) {
            MutableMessageChannel messageChannel = new PermissionMessageChannel(permissions.getPermissionWithSuffix("notify")).asMutable();
            messageChannel.addMember(src);

            if (optDuration.isPresent()) {
                String time = Util.getTimeStringFromSeconds(optDuration.get());
                messageChannel.send(plugin.getMessageProvider().getTextMessageWithFormat("command.warn.success.time", user.getName(), src.getName(), warnData.getReason(), time));

                if (user.isOnline()) {
                    user.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("warn.playernotify.time", warnData.getReason(), time));
                }
            } else {
                messageChannel.send(plugin.getMessageProvider().getTextMessageWithFormat("command.warn.success.norm", user.getName(), src.getName(), warnData.getReason()));

                if (user.isOnline()) {
                    user.getPlayer().get().sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("warn.playernotify.standard", warnData.getReason()));
                }
            }

            //Check if the subject has action command should be executed
            if (wca.getNodeOrDefault().getWarningsBeforeAction() != -1) {
                if (warnHandler.getWarningsInternal(user, true, false).size() < wca.getNodeOrDefault().getWarningsBeforeAction()) {
                    return CommandResult.success();
                }

                //Expire all active warnings
                // The cause is the plugin, as this isn't directly the warning user.
                warnHandler.clearWarnings(user, false, false, Cause.of(NamedCause.owner(NucleusPlugin.getNucleus())));

                //Get and run the action command
                String command = wca.getNodeOrDefault().getActionCommand().replaceAll("\\{\\{name}}", user.getName());
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
            }

            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.warn.fail", user.getName());
    }
}
