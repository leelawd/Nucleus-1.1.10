/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers.util;

import io.github.nucleuspowered.nucleus.util.Action;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;

public abstract class NucleusProcessing {

    public static final Text PRE_RUN_KEY = Text.of("nucleus:on-pre-run");
    public static final Text SUCCESS_KEY = Text.of("nucleus:on-success");

    public static void addToContextOnPre(CommandContext postProcess, Action action) {
        postProcess.putArg(PRE_RUN_KEY, action);
    }

    public static void addToContextOnSuccess(CommandContext postProcess, Action action) {
        postProcess.putArg(SUCCESS_KEY, action);
    }
}
