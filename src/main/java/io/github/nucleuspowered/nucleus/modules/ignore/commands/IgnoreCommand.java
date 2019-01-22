/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ignore.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.ignore.datamodules.IgnoreUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

import javax.inject.Inject;

@RunAsync
@NoModifiers
@RegisterCommand("ignore")
@Permissions(suggestedLevel = SuggestedLevel.USER)
@EssentialsEquivalent("ignore")
@NonnullByDefault
public class IgnoreCommand extends AbstractCommand<Player> {

    private final UserDataManager loader;

    private final String userKey = "user";
    private final String toggleKey = "toggle";

    @Inject
    public IgnoreCommand(UserDataManager loader) {
        this.loader = loader;
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = Maps.newHashMap();
        m.put("exempt.chat", PermissionInformation.getWithTranslation("permission.ignore.chat", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.user(Text.of(userKey))),
                GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggleKey))))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the target
        User target = args.<User>getOne(userKey).get();

        if (target.equals(src)) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ignore.self"));
            return CommandResult.empty();
        }

        IgnoreUserDataModule inu = loader.get(src).get().get(IgnoreUserDataModule.class);

        if (permissions.testSuffix(target, "exempt.chat")) {
            // Make sure they are removed.
            inu.removeFromIgnoreList(target.getUniqueId());
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ignore.exempt", target.getName()));
            return CommandResult.empty();
        }

        // Ok, we can ignore or unignore them.
        boolean ignore = args.<Boolean>getOne(toggleKey).orElse(!inu.getIgnoreList().contains(target.getUniqueId()));

        if (ignore) {
            inu.addToIgnoreList(target.getUniqueId());
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ignore.added", target.getName()));
        } else {
            inu.removeFromIgnoreList(target.getUniqueId());
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.ignore.remove", target.getName()));
        }

        return CommandResult.success();
    }
}
