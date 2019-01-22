/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.GameProfileArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.ban.Ban;

import java.util.Optional;

@RegisterCommand("checkban")
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@RunAsync
public class CheckBanCommand extends AbstractCommand<CommandSource> {

    private final String key = "uuid";
    private final String key2 = "user";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.firstParsing(
                    GenericArguments.onlyOne(UUIDArgument.gameProfile(Text.of(key))),
                    GenericArguments.onlyOne(new GameProfileArgument(Text.of(key2)))
                )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        GameProfile gp;
        if (args.hasAny(key)) {
            gp = args.<GameProfile>getOne(key).get();
        } else {
            gp = args.<GameProfile>getOne(key2).get();
        }

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        Optional<Ban.Profile> obp = service.getBanFor(gp);
        if (!obp.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkban.notset", gp.getName().orElse(plugin.getMessageProvider().getMessageWithFormat("standard.unknown"))));
            return CommandResult.success();
        }

        Ban.Profile bp = obp.get();

        String name;
        if (bp.getBanSource().isPresent()) {
            name = bp.getBanSource().get().toPlain();
        } else {
            name = plugin.getMessageProvider().getMessageWithFormat("standard.unknown");
        }

        if (bp.getExpirationDate().isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkban.bannedfor",
                    gp.getName().orElse(plugin.getMessageProvider().getMessageWithFormat("standard.unknown")), name,
                    Util.getTimeToNow(bp.getExpirationDate().get())));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkban.bannedperm",
                    gp.getName().orElse(plugin.getMessageProvider().getMessageWithFormat("standard.unknown")), name));
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.checkban.created", Util.FULL_TIME_FORMATTER.withLocale(src.getLocale())
                .format(bp.getCreationDate()
        )));
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("standard.reason", TextSerializers.FORMATTING_CODE.serialize(bp.getReason().orElse(plugin.getMessageProvider().getTextMessageWithFormat("ban.defaultreason")))));
        return CommandResult.success();
    }
}
