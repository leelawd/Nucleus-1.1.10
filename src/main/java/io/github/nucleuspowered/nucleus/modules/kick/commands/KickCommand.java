/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kick.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@RegisterCommand("kick")
@EssentialsEquivalent("kick")
@NonnullByDefault
public class KickCommand extends AbstractCommand<CommandSource> {

    private final String player = "subject";
    private final String reason = "reason";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.player(Text.of(player))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.remainingJoinedStrings(Text.of(reason))))
        };
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.kick.exempt.target", SuggestedLevel.MOD));
        m.put("notify", PermissionInformation.getWithTranslation("permission.kick.notify", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(player).get();
        String r = args.<String>getOne(reason).orElse(plugin.getMessageProvider().getMessageWithFormat("command.kick.defaultreason"));

        if (permissions.testSuffix(pl, "exempt.target", src, false)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.kick.exempt", pl.getName()));
        }

        pl.kick(TextSerializers.FORMATTING_CODE.deserialize(r));

        MessageChannel mc = new PermissionMessageChannel(permissions.getPermissionWithSuffix("notify"));
        mc.send(plugin.getMessageProvider().getTextMessageWithFormat("command.kick.message", pl.getName(), src.getName()));
        mc.send(plugin.getMessageProvider().getTextMessageWithFormat("command.reason", r));
        return CommandResult.success();
    }
}
