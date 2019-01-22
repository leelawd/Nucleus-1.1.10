/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.argumentparsers.BoundedIntegerArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanArgument;
import io.github.nucleuspowered.nucleus.internal.MixinConfigProxy;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import io.github.nucleuspowered.nucleus.modules.world.commands.border.gen.EnhancedGeneration;
import io.github.nucleuspowered.nucleus.modules.world.config.WorldConfigAdapter;
import io.github.nucleuspowered.nucleus.util.TriFunction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@Permissions(prefix = "world.border")
@RegisterCommand(value = {"gen", "genchunks", "generatechunks", "chunkgen"}, subcommandOf = BorderCommand.class)
@SkipOnError
public class GenerateChunksCommand extends AbstractCommand<CommandSource> {

    private final String worldKey = "world";
    public static final String ticksKey = "tickPercent";
    public static final String saveTimeKey = "time between saves";

    @Inject
    private WorldHelper worldHelper;

    @Inject
    private WorldConfigAdapter worldConfigAdapter;

    private final TriFunction<World, CommandSource, CommandContext, CommandResult> standardGeneration = (world, source, args) -> {
        // Create the task.
        this.worldHelper.startPregenningForWorld(world, args.hasAny("a"), args.<Integer>getOne(ticksKey).orElse(null));

        if (args.hasAny(saveTimeKey)) {
            source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gen.using.nosave"));
        }

        source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gen.using.standard"));
        source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gen.started", world.getProperties().getWorldName()));

        return CommandResult.success();
    };

    private TriFunction<World, CommandSource, CommandContext, CommandResult> generator;

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("notify", PermissionInformation.getWithTranslation("permission.world.border.gen.notify", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                    .flag("s")
                    .flag("a")
                    .valueFlag(new TimespanArgument(Text.of(saveTimeKey)), "-save")
                    .valueFlag(new BoundedIntegerArgument(Text.of(ticksKey), 0, 100), "t", "-tickpercent")
                    .buildWith(
                        GenericArguments.optional(
                            GenericArguments.onlyOne(new NucleusWorldPropertiesArgument(Text.of(worldKey), NucleusWorldPropertiesArgument.Type.ENABLED_ONLY))))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        setupGenerationClass();
        WorldProperties wp = getWorldFromUserOrArgs(src, worldKey, args);
        if (worldHelper.isPregenRunningForWorld(wp.getUniqueId())) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.gen.alreadyrunning", wp.getWorldName()));
            return CommandResult.empty();
        }

        Optional<World> w = Sponge.getServer().getWorld(wp.getUniqueId());
        if (!w.isPresent()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.world.gen.notloaded", wp.getWorldName()));
            return CommandResult.empty();
        }

        if (args.hasAny("s")) {
            return standardGeneration.accept(w.get(), src, args);
        } else {
            return this.generator.accept(w.get(), src, args);
        }
    }

    // Lazy load.
    private void setupGenerationClass() {
        if (generator == null) {
            Optional<MixinConfigProxy> mixinConfigProxy = plugin.getMixinConfigIfAvailable();
            if (mixinConfigProxy.isPresent() && mixinConfigProxy.get().testWorldGen()) {
                this.generator = new EnhancedGeneration(worldHelper, permissions.getPermissionWithSuffix("notify"), worldConfigAdapter);
            } else {
                this.generator = standardGeneration;
            }
        }
    }
}
