/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as having an equivalent in Essentials.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EssentialsEquivalent {

    /**
     * The equivalent Essentials commands.
     *
     * @return The commands.
     */
    String[] value();

    /**
     * If true, indicates that the command can be considered to be a direct translation in functionality. If false, then this is the closest
     * equivalent.
     *
     * @return <code>true</code> if the command is very close to the Essentials equivalents.
     */
    boolean isExact() default true;

    /**
     * Any notes on differences.
     *
     * @return The notes.
     */
    String notes() default "";
}
