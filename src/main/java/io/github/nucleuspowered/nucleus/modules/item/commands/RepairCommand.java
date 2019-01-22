/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.DurabilityData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

@Permissions(supportsOthers = true)
@RegisterCommand({"repair", "mend"})
@EssentialsEquivalent({"repair", "fix"})
public class RepairCommand extends AbstractCommand.SimpleTargetOtherPlayer {

    @Override protected CommandResult executeWithPlayer(CommandSource src, Player pl, CommandContext args, boolean isSelf) throws Exception {
        if (pl.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            ItemStack stack = pl.getItemInHand(HandTypes.MAIN_HAND).get();
            if (stack.get(DurabilityData.class).isPresent()) {
                DurabilityData durabilityData = stack.get(DurabilityData.class).get();
                DataTransactionResult transactionResult = stack.offer(Keys.ITEM_DURABILITY, durabilityData.durability().getMaxValue());
                if (transactionResult.isSuccessful()) {
                    pl.setItemInHand(HandTypes.MAIN_HAND, stack);
                    src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.repair.success", pl.getName()));
                    return CommandResult.success();
                } else {
                    src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.repair.error"));
                    return CommandResult.empty();
                }
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.repair.error.notreparable"));
                return CommandResult.empty();
            }
        } else {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.repair.error.handempty"));
            return CommandResult.empty();
        }
    }
}
