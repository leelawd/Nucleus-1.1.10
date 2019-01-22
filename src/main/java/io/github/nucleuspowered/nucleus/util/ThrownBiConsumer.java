/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

/**
 * A function that accepts two arguments and returns nothing, but can also throw a checked exception.
 *
 * @param <A> The first argument type.
 * @param <B> The second argument type.
 * @param <X> The type of {@link Throwable} that could be thrown.
 */
@FunctionalInterface
public interface ThrownBiConsumer<A, B, X extends Throwable> {

    void accept(A a, B b) throws X;
}
