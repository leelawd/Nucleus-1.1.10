/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.powertool.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.powertool.datamodules.PowertoolUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Permissions
@RunAsync
@NoModifiers
@RegisterCommand({"powertool", "pt"})
@EssentialsEquivalent({"powertool", "pt"})
@NonnullByDefault
public class PowertoolCommand extends AbstractCommand<Player> {

    private final UserDataManager loader;
    private final String commandKey = "command";

    @Inject
    public PowertoolCommand(UserDataManager userDataManager) {
        this.loader = userDataManager;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of(commandKey)))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        ItemStack itemStack = src.getItemInHand(HandTypes.MAIN_HAND)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.powertool.noitem"));

        Optional<String> command = args.getOne(commandKey);
        PowertoolUserDataModule inu = loader.getUnchecked(src).get(PowertoolUserDataModule.class);
        return command.map(s -> setPowertool(src, inu, itemStack.getItem(), s))
                .orElseGet(() -> viewPowertool(src, inu, itemStack));
    }

    private CommandResult viewPowertool(Player src, PowertoolUserDataModule user, ItemStack item) {
        Optional<List<String>> cmds = user.getPowertoolForItem(item.getItem());
        MessageProvider mp = plugin.getMessageProvider();
        if (cmds.isPresent() && !cmds.get().isEmpty()) {
            Util.getPaginationBuilder(src)
                    .contents(cmds.get().stream().map(f -> Text.of(TextColors.YELLOW, f)).collect(Collectors.toList()))
                    .title(mp.getTextMessageWithTextFormat("command.powertool.viewcmdstitle", Text.of(item), Text.of(item.getItem().getId())))
                    .sendTo(src);
        } else {
            src.sendMessage(mp.getTextMessageWithTextFormat("command.powertool.nocmds", Text.of(item)));
        }

        return CommandResult.success();
    }

    private CommandResult setPowertool(Player src, PowertoolUserDataModule user, ItemType item, String command) {
        // For consistency, if a command starts with "/", remove it, but just
        // once. WorldEdit commands can be input using "//"
        if (command.startsWith("/")) {
            command = command.substring(1);
        }

        user.setPowertool(item, Lists.newArrayList(command));
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.powertool.set", item.getId(), command));
        return CommandResult.success();
    }
}
