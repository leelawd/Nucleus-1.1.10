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
import org.spongepowered.api.world.gen.WorldGeneratorModifier;

@NonnullByDefault
@NoModifiers
@RegisterCommand(value = {"modifiers", "listmodifiers"}, subcommandOf = WorldCommand.class)
@Permissions(prefix = "world", mainOverride = "create")
public class AvailableModifiersCommand extends AvailableBaseCommand {

    public AvailableModifiersCommand() {
        super(WorldGeneratorModifier.class, "command.world.modifiers.title");
    }

}
