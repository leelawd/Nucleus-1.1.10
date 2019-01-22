/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Scan;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.util.ThrownBiConsumer;
import io.github.nucleuspowered.nucleus.util.TriConsumer;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.storage.WorldProperties;

import javax.annotation.Nullable;

@Scan
public class PropertiesWorldCommands {

    private static final String worldKey = "world";
    private static final String valueKey = "true|false";

    private static abstract class AbstractSetCommand extends AbstractCommand<CommandSource> {

        private final ThrownBiConsumer<WorldProperties, Boolean, Exception>  setter;
        private final String name;
        private final TriConsumer<CommandSource, WorldProperties, Boolean> extraLogic;

        private AbstractSetCommand(String name, ThrownBiConsumer<WorldProperties, Boolean, Exception> setter) {
            this(name, setter, null);
        }

        private AbstractSetCommand(String name, ThrownBiConsumer<WorldProperties, Boolean, Exception> setter, @Nullable
            TriConsumer<CommandSource, WorldProperties, Boolean> extraLogic) {

            super();
            this.name = name;
            this.setter = setter;
            this.extraLogic = extraLogic;
        }

        @Override public CommandElement[] getArguments() {
            return new CommandElement[] {
                GenericArguments.optionalWeak(new NucleusWorldPropertiesArgument(Text.of(worldKey), NucleusWorldPropertiesArgument.Type.ALL)),
                GenericArguments.bool(Text.of(valueKey))
            };
        }

        @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            WorldProperties worldProperties = getWorldPropertiesOrDefault(src, worldKey, args);
            boolean set = args.<Boolean>getOne(valueKey).get();
            setter.accept(worldProperties, set);
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.setproperty.success", name, worldProperties.getWorldName(), String.valueOf(set)));

            if (extraLogic != null) {
                extraLogic.accept(src, worldProperties, set);
            }

            return CommandResult.success();
        }
    }

    @Permissions(prefix = "world")
    @RegisterCommand(value = "sethardcore", subcommandOf = WorldCommand.class)
    public static class SetHardcoreCommand extends AbstractSetCommand {

        public SetHardcoreCommand() {
            super("hardcore", WorldProperties::setHardcore, (cs, wp, set) -> {
                if (!set) {
                    cs.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.sethardcore.diff", wp.getDifficulty().getName()));
                }
            });
        }
    }

    @Permissions(prefix = "world")
    @RegisterCommand(value = "setloadonstartup", subcommandOf = WorldCommand.class)
    public static class SetLoadOnStartup extends AbstractSetCommand {

        public SetLoadOnStartup() {
            super("load on startup", WorldProperties::setLoadOnStartup);
        }
    }

    @Permissions(prefix = "world")
    @RegisterCommand(value = {"setpvpenabled", "setpvp"}, subcommandOf = WorldCommand.class)
    public static class SetPvpEnabled extends AbstractSetCommand {

        public SetPvpEnabled() {
            super("pvp", WorldProperties::setPVPEnabled);
        }
    }

    @Permissions(prefix = "world")
    @RegisterCommand(value = "setkeepspawnloaded", subcommandOf = WorldCommand.class)
    public static class SetKeepSpawnLoaded extends AbstractSetCommand {

        public SetKeepSpawnLoaded() {
            super("keep spawn loaded", WorldProperties::setKeepSpawnLoaded);
        }
    }
}
