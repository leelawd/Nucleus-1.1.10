/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.dataproviders;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;

class FileChangingConfigurateDataProvider<T> implements DataProvider.FileChanging<T> {

    private final Supplier<DataProvider<T>> providerSupplier;
    private DataProvider<T> provider;

    @SuppressWarnings("unchecked") FileChangingConfigurateDataProvider(TypeToken<T> type, Function<Path, ConfigurationLoader<?>> loaderProvider, Supplier<Path> file, Logger logger) {
        this(type, loaderProvider, () -> {
            try {
                return (T)type.getRawType().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }, file, true, logger);
    }

    FileChangingConfigurateDataProvider(TypeToken<T> type, Function<Path, ConfigurationLoader<?>>  loaderProvider, Supplier<T> defaultSupplier, Supplier<Path> file, boolean requiresChildren, Logger logger) {
        this.providerSupplier = () -> new ConfigurateDataProvider<>(type, loaderProvider, defaultSupplier, file.get(), requiresChildren, logger);
    }

    public void onChange() {
        this.provider = this.providerSupplier.get();
    }

    @Override
    public boolean has() {
        return this.provider.has();
    }

    @Override
    public T load() throws Exception {
        return this.provider.load();
    }

    @Override
    public void save(T info) throws Exception {
        this.provider.save(info);
    }

    @Override
    public void delete() throws Exception {
        this.provider.delete();
    }
}
