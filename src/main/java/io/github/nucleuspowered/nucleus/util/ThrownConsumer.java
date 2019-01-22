/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

/**
 * A function that accepts an argument and returns nothing, but can also throw a checked exception.
 *
 * @param <A> The argument type.
 * @param <X> The type of {@link Throwable} that could be thrown.
 */
@FunctionalInterface
public interface ThrownConsumer<A, X extends Throwable> {

    void accept(A a) throws X;
}
