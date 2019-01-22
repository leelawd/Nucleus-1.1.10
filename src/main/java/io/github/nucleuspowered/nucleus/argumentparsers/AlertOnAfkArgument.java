/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.util.NucleusProcessing;
import io.github.nucleuspowered.nucleus.argumentparsers.util.WrappedElement;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.modules.afk.AFKModule;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

public class AlertOnAfkArgument extends WrappedElement {

    @Nullable private static AFKConfig config = null;
    private static boolean isFirst = true;

    public AlertOnAfkArgument(CommandElement element) {
        super(element);
        if (isFirst) {
            Nucleus.getNucleus().registerReloadable(() ->
                    config = Nucleus.getNucleus().getConfigValue(AFKModule.ID, AFKConfigAdapter.class, x -> x).orElse(null));
            isFirst = false;
        }
    }

    public static void getAction(CommandSource source, Player player) {
        if (config != null && config.isAlertSenderOnAfk()) {
            NucleusTextTemplateImpl onCommand = config.getMessages().getOnCommand();
            if (!onCommand.isEmpty()) {
                source.sendMessage(onCommand.getForCommandSource(player));
            }
        }
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        getWrappedElement().parse(source, args, context);

        if (config != null && config.isAlertSenderOnAfk()) {
            try {
                Collection<Player> players = context.getAll(getWrappedElement().getKey());

                Nucleus.getNucleus().getInternalServiceManager().getService(AFKHandler.class)
                    .ifPresent(x -> players.forEach(y -> {
                        if (x.isAFK(y)) {
                            NucleusProcessing.addToContextOnSuccess(context, () -> getAction(source, y));
                        }
                    }));
            } catch (Throwable e) {
                if (Nucleus.getNucleus().isDebugMode()) {
                    Nucleus.getNucleus().getLogger().warn("AFK argument is on a non-player argument");
                }
            }
        }
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return getWrappedElement().complete(src, args, context);
    }
}
