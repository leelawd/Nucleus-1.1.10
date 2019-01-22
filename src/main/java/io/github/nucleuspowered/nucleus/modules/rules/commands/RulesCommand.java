/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rules.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.rules.RulesModule;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfig;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand("rules")
@NoModifiers
@NonnullByDefault
@EssentialsEquivalent("rules")
public class RulesCommand extends AbstractCommand<CommandSource> implements StandardAbstractCommand.Reloadable {

    private final RulesConfigAdapter rca;
    private Text title = Text.EMPTY;

    @Inject
    public RulesCommand(RulesConfigAdapter rca) {
        this.rca = rca;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        plugin.getTextFileController(RulesModule.RULES_KEY)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.rules.empty"))
                .sendToPlayer(src, title);
        return CommandResult.success();
    }

    @Override public void onReload() {
        RulesConfig config = rca.getNodeOrDefault();
        String title = config.getRulesTitle();
        if (title.isEmpty()) {
            this.title = Text.EMPTY;
        } else {
            this.title = TextSerializers.FORMATTING_CODE.deserialize(title);
        }
    }
}
