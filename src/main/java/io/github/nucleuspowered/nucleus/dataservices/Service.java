/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

public interface Service {

    boolean load();

    void loadInternal() throws Exception;

    boolean save();

    boolean delete();
}
