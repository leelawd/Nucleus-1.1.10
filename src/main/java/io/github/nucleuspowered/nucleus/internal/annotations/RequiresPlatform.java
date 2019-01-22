/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations;

import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginContainer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a command/listener/runnable requires a specific game platform to run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RequiresPlatform {

    /**
     * The platform(s) that is/are required.
     *
     * @return The IDs of the platforms. Obtained from {@link PluginContainer#getId()}
     */
    String[] value() default {"Minecraft"};

    /**
     * The component that the {@link #value()} depends on.
     *
     * @return The {@link Platform.Component}
     */
    Platform.Component component() default Platform.Component.GAME;
}
