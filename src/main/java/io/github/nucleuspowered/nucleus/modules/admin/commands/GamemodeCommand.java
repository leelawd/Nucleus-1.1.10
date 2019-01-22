/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.admin.config.AdminConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@Permissions
@RegisterCommand({"gamemode", "gm"})
@EssentialsEquivalent(value = {"gamemode", "gm", "creative", "survival", "adventure", "gmc", "gma", "gms", "gmt"}, isExact = false,
    notes = "Currently no way to simply give '/creative' or '/gmc', for example, with no arguments, gamemode is required.")
public class GamemodeCommand extends AbstractCommand<CommandSource> {

    private final String userKey = "user";
    private final String gamemodeKey = "gamemode";
    @Inject private AdminConfigAdapter adminConfigAdapter;

    private final Map<String, String> modeMap = new HashMap<String, String>() {{
        put(GameModes.SURVIVAL.getId(), "modes.survival");
        put(GameModes.CREATIVE.getId(), "modes.creative");
        put(GameModes.ADVENTURE.getId(), "modes.adventure");
        put(GameModes.SPECTATOR.getId(), "modes.spectator");
    }};

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mpi = Maps.newHashMap();
        mpi.put("others", PermissionInformation.getWithTranslation("permission.gamemode.other", SuggestedLevel.ADMIN));
        mpi.put("modes.survival", PermissionInformation.getWithTranslation("permission.gamemode.modes.survival", SuggestedLevel.ADMIN));
        mpi.put("modes.creative", PermissionInformation.getWithTranslation("permission.gamemode.modes.creative", SuggestedLevel.ADMIN));
        mpi.put("modes.adventure", PermissionInformation.getWithTranslation("permission.gamemode.modes.adventure", SuggestedLevel.ADMIN));
        mpi.put("modes.spectator", PermissionInformation.getWithTranslation("permission.gamemode.modes.spectator", SuggestedLevel.ADMIN));
        return mpi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.firstParsing(

                    // <mode>
                    GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(gamemodeKey))),

                    // <player> <mode>
                    GenericArguments.seq(
                        GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.player(Text.of(userKey))), permissions.getPermissionWithSuffix("others")),
                        GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(gamemodeKey))))
                )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player user = this.getUserFromArgs(Player.class, src, userKey, args);
        Optional<GameMode> ogm = args.getOne(gamemodeKey);
        if (!ogm.isPresent()) {
            String mode = user.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL).getName();
            if (src.equals(user)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.gamemode.get.base", mode));
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.gamemode.get.other", user.getName(), mode));
            }

            return CommandResult.success();
        }

        GameMode gm = ogm.get();

        // We now check for the correct permissions for the game mode we are switching to, if set in config.
        if (adminConfigAdapter.getNodeOrDefault().isSeparateGamemodePermission()
                && !permissions.testSuffix(src, modeMap.computeIfAbsent(
            gm.getId(), key -> {
                String[] keySplit = key.split(":", 2);
                String r = keySplit[keySplit.length - 1].toLowerCase();
                modeMap.put(key, "modes." + r);
                return "modes." + r;
            }
        ))) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.gamemode.permission", gm.getTranslation().get()));
        }

        DataTransactionResult dtr = user.offer(Keys.GAME_MODE, gm);
        if (dtr.isSuccessful()) {
            if (!src.equals(user)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.gamemode.set.other", user.getName(), gm.getName()));
            }

            user.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.gamemode.set.base", gm.getName()));
            return CommandResult.success();
        }

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.gamemode.error", user.getName()));
        return CommandResult.empty();
    }
}
