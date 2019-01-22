/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.message.config.MessageConfig;
import io.github.nucleuspowered.nucleus.modules.message.config.MessageConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.message.events.InternalNucleusHelpOpEvent;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@RunAsync
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand({"helpop"})
@EssentialsEquivalent({"helpop", "amsg", "ac"})
public class HelpOpCommand extends AbstractCommand<Player> implements StandardAbstractCommand.Reloadable {

    private final String messageKey = "message";

    @Inject private MessageConfigAdapter mca;
    private MessageConfig messageConfig;
    @Inject private TextParsingUtils textParsingUtils;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.remainingJoinedStrings(Text.of(messageKey))};
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("receive", PermissionInformation.getWithTranslation("permission.helpop.receive", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        String message = args.<String>getOne(messageKey).get();

        // Message is about to be sent. Send the event out. If canceled, then
        // that's that.
        if (Sponge.getEventManager().post(new InternalNucleusHelpOpEvent(src, message))) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("message.cancel"));
            return CommandResult.empty();
        }

        Text prefix = messageConfig.getHelpOpPrefix().getForCommandSource(src);

        new PermissionMessageChannel(permissions.getPermissionWithSuffix("receive"))
                .send(src, TextParsingUtils.joinTextsWithColoursFlowing(prefix, Text.of(message)));

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.helpop.success"));

        return CommandResult.success();
    }

    @Override public void onReload() {
        messageConfig = mca.getNodeOrDefault();
    }
}
