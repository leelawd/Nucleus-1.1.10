/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

public interface DataProvider<T> {

    boolean has();

    T load() throws Exception;

    void save(T info) throws Exception;

    void delete() throws Exception;

    interface FileChanging<T> extends DataProvider<T> {

        void onChange();
    }

}
