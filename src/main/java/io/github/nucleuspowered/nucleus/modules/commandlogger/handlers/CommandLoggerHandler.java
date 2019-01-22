/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandlogger.handlers;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.logging.AbstractLoggingHandler;
import io.github.nucleuspowered.nucleus.modules.commandlogger.config.CommandLoggerConfigAdapter;

import javax.inject.Inject;

public class CommandLoggerHandler extends AbstractLoggingHandler {

    private final CommandLoggerConfigAdapter clca;

    @Inject
    public CommandLoggerHandler(NucleusPlugin plugin, CommandLoggerConfigAdapter clca) {
        super(plugin, "command", "cmds");
        this.clca = clca;
    }

    public void onReload() throws Exception {
        if (clca.getNodeOrDefault().isLogToFile() && logger == null) {
            this.createLogger();
        } else if (!clca.getNodeOrDefault().isLogToFile() && logger != null) {
            onShutdown();
        }
    }

    @Override protected boolean enabledLog() {
        return clca.getNodeOrDefault().isLogToFile();
    }
}
