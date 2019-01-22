/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.lists;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.modules.world.commands.WorldCommand;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.WorldArchetype;

@NonnullByDefault
@NoModifiers
@RegisterCommand(value = {"presets", "listpresets"}, subcommandOf = WorldCommand.class)
@Permissions(prefix = "world", mainOverride = "create")
public class AvailablePresetsCommand extends AvailableBaseCommand {

    public AvailablePresetsCommand() {
        super(WorldArchetype.class, "command.world.presets.title");
    }

}
