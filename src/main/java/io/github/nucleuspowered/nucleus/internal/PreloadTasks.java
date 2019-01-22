/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public abstract class PreloadTasks {

    private PreloadTasks() {}

    @SuppressWarnings("unchecked")
    public static List<Consumer<NucleusPlugin>> getPreloadTasks() {
        return Lists.newArrayList(
            // Move the modules location
            plugin -> {
                try {
                    // Get the main conf file, move the "modules" section
                    Path main = plugin.getConfigDirPath().resolve("main.conf");
                    if (!Files.exists(main)) {
                        return;
                    }

                    HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setPath(main).build();
                    CommentedConfigurationNode node = loader.load();
                    CommentedConfigurationNode c = node.getNode("modules");
                    if (!c.isVirtual()) {
                        node.getNode("-modules").setValue(c);
                        node.getNode("modules").setValue(null);
                    }

                    // Remove the blacklist node.
                    node.getNode("-modules", "blacklist").setValue(null);
                    node.getNode("blacklist").setValue(null);
                    loader.save(node);
                } catch (Exception ignored) {
                    // ignored
                }
            });
    }

    @SuppressWarnings("unchecked")
    public static List<Consumer<NucleusPlugin>> getPreloadTasks2() {
        return Lists.newArrayList();
    }
}
