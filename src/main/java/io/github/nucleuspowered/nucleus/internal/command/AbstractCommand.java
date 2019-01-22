/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * A basic {@link CommandExecutor} that does not attempt to cache permission calls.
 *
 * @param <T> The type of {@link CommandSource}.
 */
@NonnullByDefault
public abstract class AbstractCommand<T extends CommandSource> extends StandardAbstractCommand<T> {

    @Override
    protected final CommandResult executeCommand(SubjectPermissionCache<T> src, CommandContext args) throws Exception {
        return executeCommand(src.getSubject(), args);
    }

    /**
     * Functionally similar to
     * {@link CommandExecutor#execute(CommandSource, CommandContext)}, this
     * contains logic that actually executes the command.
     *
     * <p> Note that the {@link CommandResult} is important here. A success is
     * treated differently to a non-success! </p>
     *
     * @param src The executor of the command.
     * @param args The arguments for the command.
     * @return The {@link CommandResult}
     * @throws Exception If thrown, {@link TextMessageException#getText()} or
     *         {@link Exception#getMessage()} will be sent to the user.
     */
    protected abstract CommandResult executeCommand(T src, CommandContext args) throws Exception;

    @NonnullByDefault
    public abstract static class SimpleTargetOtherPlayer extends StandardAbstractCommand.SimpleTargetOtherPlayer {

        @Override protected CommandResult executeWithPlayer(SubjectPermissionCache<CommandSource> source, Player target, CommandContext args, boolean isSelf)
                throws Exception {
            return executeWithPlayer(source.getSubject(), target, args, isSelf);
        }

        protected abstract CommandResult executeWithPlayer(CommandSource source, Player target, CommandContext args, boolean isSelf) throws Exception;
    }
}
