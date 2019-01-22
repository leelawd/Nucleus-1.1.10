/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

@RegisterCommand({"afk", "away"})
@Permissions(suggestedLevel = SuggestedLevel.USER)
@NoModifiers
@RunAsync
@EssentialsEquivalent({"afk", "away"})
@NonnullByDefault
public class AFKCommand extends AbstractCommand<Player> {

    private final AFKHandler afkHandler;

    @Inject
    public AFKCommand(AFKHandler afkHandler) {
        this.afkHandler = afkHandler;
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.toggle", PermissionInformation.getWithTranslation("permission.afk.exempt.toggle", SuggestedLevel.NONE));
        m.put("exempt.kick", PermissionInformation.getWithTranslation("permission.afk.exempt.kick", SuggestedLevel.ADMIN));
        m.put("notify", PermissionInformation.getWithTranslation("permission.afk.notify", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (permissions.testSuffix(src, "exempt.toggle")) {
            throw ReturnMessageException.fromKey("command.afk.exempt");
        }

        boolean isAFK = afkHandler.isAfk(src);

        if (isAFK) {
            afkHandler.stageUserActivityUpdate(src);
        } else if (!afkHandler.setAfk(src, Cause.of(NamedCause.owner(src)), true)) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.afk.notset"));
        }

        return CommandResult.success();
    }
}
