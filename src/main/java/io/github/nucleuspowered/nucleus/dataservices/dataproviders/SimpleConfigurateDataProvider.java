/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.Function;

public class SimpleConfigurateDataProvider extends AbstractConfigurateDataProvider<ConfigurationNode> {

    public SimpleConfigurateDataProvider(
        Function<Path, ConfigurationLoader<?>> loaderProvider, Path file, boolean requiresChildren, Logger logger) {

        super(loaderProvider, file, requiresChildren, logger);
    }

    @Override protected ConfigurationNode transformOnLoad(ConfigurationNode node) throws Exception {
        return node;
    }

    @Override protected ConfigurationNode transformOnSave(ConfigurationNode info) throws Exception {
        return info;
    }
}
