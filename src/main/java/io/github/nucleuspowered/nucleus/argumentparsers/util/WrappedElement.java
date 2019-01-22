/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers.util;

import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

public abstract class WrappedElement extends CommandElement {

    private final CommandElement wrappedElement;

    public WrappedElement(CommandElement wrappedElement) {
        super(wrappedElement.getKey());
        this.wrappedElement = wrappedElement;
    }

    public CommandElement getWrappedElement() {
        return this.wrappedElement;
    }

    @Nullable @Override public Text getKey() {
        return this.wrappedElement.getKey();
    }
}
