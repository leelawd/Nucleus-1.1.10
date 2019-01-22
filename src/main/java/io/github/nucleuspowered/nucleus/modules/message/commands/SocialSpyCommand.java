/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoModifiers
@RegisterCommand("socialspy")
@EssentialsEquivalent("socialspy")
@NonnullByDefault
public class SocialSpyCommand extends AbstractCommand<Player> {

    private final String arg = "Social Spy";
    private final MessageHandler handler;

    @Inject
    public SocialSpyCommand(MessageHandler handler) {
        this.handler = handler;
    }

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("force", PermissionInformation.getWithTranslation("permission.socialspy.force", SuggestedLevel.NONE));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(arg))))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (handler.forcedSocialSpyState(src).asBoolean()) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.socialspy.forced"));
        }

        boolean spy = args.<Boolean>getOne(arg).orElse(!handler.isSocialSpy(src));
        if (handler.setSocialSpy(src, spy)) {
            Text message = plugin.getMessageProvider().getTextMessageWithFormat(spy ? "command.socialspy.on" : "command.socialspy.off");
            src.sendMessage(message);
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.socialspy.unable"));
        return CommandResult.empty();
    }
}
