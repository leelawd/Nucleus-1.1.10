/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.geoip.commands;

import com.maxmind.geoip2.record.Country;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.geoip.handlers.GeoIpDatabaseHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@RunAsync
@NoModifiers
@Permissions
@RegisterCommand("geoip")
@NonnullByDefault
public class GeoIpCommand extends AbstractCommand<CommandSource> {

    private final GeoIpDatabaseHandler databaseHandler;
    private final UserDataManager userDataManager;

    private final String playerKey = "subject";

    @Inject
    public GeoIpCommand(GeoIpDatabaseHandler databaseHandler, UserDataManager userDataManager) {
        this.databaseHandler = databaseHandler;
        this.userDataManager = userDataManager;
    }

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("login", PermissionInformation.getWithTranslation("permission.geoip.login", SuggestedLevel.ADMIN));
        }};
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new NicknameArgument<>(Text.of(playerKey), NicknameArgument.UnderlyingType.PLAYER)
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player player = args.<Player>getOne(playerKey).get();
        Optional<Country> country = databaseHandler.getDetails(player.getConnection().getAddress().getAddress()).get();
        if (country.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("geoip.playerfrom", player.getName(), country.get().getName()));
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("geoip.noinfo", player.getName()));
        }

        return CommandResult.success();
    }
}
