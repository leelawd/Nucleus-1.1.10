/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config.bases;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;

public abstract class AbstractConfig<T extends ConfigurationNode, L extends ConfigurationLoader<T>> {

    public abstract void load() throws Exception;

    public abstract void save() throws IOException;

    protected abstract L getLoader(Path file);
}
