/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import org.spongepowered.api.command.CommandResult;

public enum ContinueMode {
    /**
     * Continue executing the command.
     */
    CONTINUE(true, null),

    /**
     * Stop executing, but mark as success.
     */
    STOP_SUCCESS(false, CommandResult.success()),

    /**
     * Stop executing, mark as empty.
     */
    STOP(false, CommandResult.empty());

    final boolean cont;
    final CommandResult returnType;

    ContinueMode(boolean cont, CommandResult returnType) {
        this.cont = cont;
        this.returnType = returnType;
    }
}
