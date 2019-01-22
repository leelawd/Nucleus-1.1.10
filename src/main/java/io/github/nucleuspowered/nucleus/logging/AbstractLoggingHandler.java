/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.logging;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import org.spongepowered.api.GameState;
import org.spongepowered.api.Sponge;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public abstract class AbstractLoggingHandler {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());
    protected DateRotatableFileLogger logger;
    private final List<String> queueEntry = Lists.newArrayList();
    private final NucleusPlugin plugin;
    private final String directoryName;
    private final String filePrefix;
    private final Object locking = new Object();

    @Inject
    public AbstractLoggingHandler(NucleusPlugin plugin, String directoryName, String filePrefix) {
        this.plugin = plugin;
        this.directoryName = directoryName;
        this.filePrefix = filePrefix;
    }

    public void queueEntry(String s) {
        if (logger != null) {
            synchronized (locking) {
                queueEntry.add(s);
            }
        }
    }

    public abstract void onReload() throws Exception;

    public void onServerShutdown() throws IOException {
        Preconditions.checkState(Sponge.getGame().getState().equals(GameState.SERVER_STOPPED));
        onShutdown();
    }

    protected void onShutdown() throws IOException {
        if (logger != null) {
            logger.close();
            logger = null;
        }
    }

    protected abstract boolean enabledLog();

    public void onTick() {
        if (queueEntry.isEmpty()) {
            return;
        }

        List<String> l;
        synchronized (locking) {
            l = Lists.newArrayList(queueEntry);
            queueEntry.clear();
        }

        if (logger == null) {
            if (enabledLog()) {
                try {
                    createLogger();
                } catch (IOException e) {
                    plugin.getLogger().warn(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("commandlog.couldnotwrite"));
                    if (plugin.isDebugMode()) {
                        e.printStackTrace();
                    }

                    return;
                }
            } else {
                return;
            }
        }

        try {
            writeEntry(l);
        } catch (IOException e) {
            plugin.getLogger().warn(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("commandlog.couldnotwrite"));
            if (plugin.isDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    protected void createLogger() throws IOException {
        logger = new DateRotatableFileLogger(directoryName, filePrefix, s -> "[" +
            formatter.format(Instant.now().atZone(ZoneOffset.systemDefault())) +
            "] " + s);
    }

    private void writeEntry(Iterable<String> entry) throws IOException {
        if (logger != null) {
            logger.logEntry(entry);
        }
    }
}
