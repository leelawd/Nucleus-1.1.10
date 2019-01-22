/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

/**
 * A function that accepts four arguments and returns a result.
 *
 * @param <A> The first argument type.
 * @param <B> The second argument type.
 * @param <C> The third argument type.
 * @param <D> The foruth argument type.
 * @param <R> The type of the result.
 */
@FunctionalInterface
public interface QuadFunction<A, B, C, D, R> {

    R accept(A a, B b, C c, D d);
}
