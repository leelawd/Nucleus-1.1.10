/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import com.google.common.collect.Sets;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.Sponge;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class CommandBuilder {

    private final NucleusPlugin plugin;
    private final Injector injector;
    private final Set<Class<? extends StandardAbstractCommand<?>>> commandSet;
    private final SimpleCommentedConfigurationNode sn;
    private final String moduleID;
    private final String moduleName;

    private final static Set<Class<? extends StandardAbstractCommand>> registeredCommands = Sets.newHashSet();

    public static boolean isCommandRegistered(Class<? extends StandardAbstractCommand> command) {
        return registeredCommands.contains(command);
    }

    public CommandBuilder(NucleusPlugin plugin, Injector injector, Set<Class<? extends StandardAbstractCommand<?>>> commandSet, String moduleID, String moduleName) {
        this.plugin = plugin;
        this.injector = injector;
        this.commandSet = commandSet;
        this.sn = SimpleCommentedConfigurationNode.root();
        this.moduleID = moduleID;
        this.moduleName = moduleName;
    }

    public <T extends StandardAbstractCommand<?>> Optional<T> buildCommand(Class<T> commandClass) {
        return buildCommand(commandClass, true);
    }

    <T extends StandardAbstractCommand<?>> Optional<T> buildCommand(Class<T> commandClass, boolean rootCmd) {
        Optional<T> optionalCommand = getInstance(commandClass);
        if (!optionalCommand.isPresent()) {
            return Optional.empty();
        }

        T c = optionalCommand.get();
        try {
            c.setModuleName(moduleID, moduleName);
            c.setModuleCommands(commandSet);
            c.setCommandBuilder(this);
            c.postInit();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }

        // If we are using DocGen, add the command information to the system.
        plugin.getDocGenCache().ifPresent(x -> x.addCommand(moduleID, c));

        String commandSection = c.getAliases()[0].toLowerCase();
        sn.getNode(commandSection).setValue(c.getDefaults());

        if (plugin.getCommandsConfig().getCommandNode(commandSection).getNode("enabled").getBoolean(true)) {
            ConfigurationNode cn = plugin.getCommandsConfig().getCommandNode(commandSection);
            ConfigurationNode node = cn.getNode("aliases");
            if (node.getValue() == null) {
                cn.removeChild("aliases");
            }

            try {
                // Register the commands.
                if (rootCmd) {
                    // This will return true for the first anyway
                    String first = c.getAliases()[0];
                    String[] aliases = Arrays.stream(c.getAliases()).filter(x -> x.equals(first) || node.getNode(x).getBoolean(true))
                            .toArray(String[]::new);
                    Sponge.getCommandManager().register(plugin, c, aliases);
                }

                // Register as another full blown command.
                for (String s : c.getRootCommandAliases()) {
                    if (cn.getNode("aliases", s).getBoolean(true)) {
                        Sponge.getCommandManager().register(plugin, c, s);
                    }
                }

                if (c instanceof StandardAbstractCommand.Reloadable) {
                    plugin.registerReloadable(((StandardAbstractCommand.Reloadable) c)::onReload);
                }
            } catch (Exception e) {
                throw new IllegalStateException(plugin.getMessageProvider().getMessageWithFormat("startup.commandfailiure", c.getAliases()[0],
                        commandClass.getName()));
            }

            registeredCommands.add(c.getClass());
            return Optional.of(c);
        }

        return Optional.empty();
    }

    private <T extends StandardAbstractCommand<?>> Optional<T> getInstance(Class<T> clazz) {
        try {
            T instance = injector.getInstance(clazz);
            if (instance.canLoad()) {
                return Optional.of(instance);
            }

            return Optional.empty();

            // I can't believe I have to do this...
        } catch (RuntimeException | NoClassDefFoundError e) {
            if (clazz.isAnnotationPresent(SkipOnError.class)) {
                plugin.getLogger().warn(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("startup.injectablenotloaded", clazz.getName()));
                return Optional.empty();
            }

            throw e;
        }
    }

    public CommentedConfigurationNode getNodeToMerge() {
        return sn;
    }
}
