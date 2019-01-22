/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

import javax.inject.Inject;

@Permissions(supportsOthers = true)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"vanish", "v"})
@EssentialsEquivalent({"vanish", "v"})
public class VanishCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    private final UserDataManager userDataManager;

    private final String b = "toggle";

    @Inject
    public VanishCommand(UserDataManager userDataManager) {
        this.userDataManager = userDataManager;
    }

    @Override public CommandElement[] additionalArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(b))))
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = Maps.newHashMap();
        mspi.put("see", PermissionInformation.getWithTranslation("permission.vanish.see", SuggestedLevel.ADMIN));
        mspi.put("persist", PermissionInformation.getWithTranslation("permission.vanish.persist", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override
    public CommandResult executeWithPlayer(CommandSource src, Player playerToVanish, CommandContext args, boolean isSelf) throws Exception {
        if (playerToVanish.getPlayer().isPresent()) {
            return onPlayer(src, args, playerToVanish.getPlayer().get());
        }

        if (!permissions.testSuffix(playerToVanish, "persist")) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.noperm", playerToVanish.getName()));
        }

        VanishUserDataModule uss = userDataManager.get(playerToVanish).get().get(VanishUserDataModule.class);
        uss.setVanished(args.<Boolean>getOne(b).orElse(!uss.isVanished()));

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.successuser",
            playerToVanish.getName(),
            uss.isVanished() ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") : plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));

        return CommandResult.success();
    }

    private CommandResult onPlayer(CommandSource src, CommandContext args, Player playerToVanish) throws Exception {
        // If we don't specify whether to vanish, toggle
        boolean toVanish = args.<Boolean>getOne(b).orElse(!playerToVanish.get(Keys.VANISH).orElse(false));

        userDataManager.get(playerToVanish).get().get(VanishUserDataModule.class).setVanished(toVanish);
        if (!playerToVanish.get(Keys.GAME_MODE).orElse(GameModes.NOT_SET).equals(GameModes.SPECTATOR)) {
            DataTransactionResult dtr = playerToVanish.offer(Keys.VANISH, toVanish);
            playerToVanish.offer(Keys.VANISH_PREVENTS_TARGETING, toVanish);
            playerToVanish.offer(Keys.VANISH_IGNORES_COLLISION, toVanish);
            playerToVanish.offer(Keys.IS_SILENT, toVanish);

            if (!dtr.isSuccessful()) {
                throw ReturnMessageException.fromKey("command.vanish.fail");
            }
        }

        playerToVanish.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.success",
            toVanish ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") :
                plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));

        if (!(src instanceof Player) || !(((Player) src).getUniqueId().equals(playerToVanish.getUniqueId()))) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.vanish.successplayer",
                TextSerializers.FORMATTING_CODE.serialize(plugin.getNameUtil().getName(playerToVanish)),
                toVanish ? plugin.getMessageProvider().getMessageWithFormat("command.vanish.vanished") :
                    plugin.getMessageProvider().getMessageWithFormat("command.vanish.visible")));
        }

        return CommandResult.success();
    }
}
