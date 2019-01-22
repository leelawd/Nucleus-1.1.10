/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.settingprocessor;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@link SettingProcessor} to pull out valid living entities from a list.
 */
public class MobTypeSettingProcessor implements SettingProcessor {

    private final static TypeToken<String> stringTypeToken = TypeToken.of(String.class);
    private final static TypeToken<List<EntityType>> entityTypeToken =
            new TypeToken<List<EntityType>>() {};

    @Override
    public void process(ConfigurationNode cn) throws ObjectMappingException {
        List<EntityType> types = Sponge.getRegistry().getAllOf(EntityType.class).stream()
                .filter(x -> Living.class.isAssignableFrom(x.getEntityClass()))
                .collect(Collectors.toList());
        List<EntityType> whitelist = Lists.newArrayList();
        cn.getList(stringTypeToken).forEach(x -> {
            if (x.contains(":")) {
                types.stream().filter(y -> y.getId().equalsIgnoreCase(x)).findFirst()
                        .ifPresent(whitelist::add);
            } else {
                String potentialId = "minecraft:" + x.toLowerCase();
                Optional<EntityType> typeOptional =
                        types.stream().filter(y -> y.getId().equalsIgnoreCase(potentialId)).findFirst();
                if (typeOptional.isPresent()) {
                    whitelist.add(typeOptional.get());
                } else {
                    types.stream().filter(y -> y.getName().equalsIgnoreCase(x)).findFirst()
                        .ifPresent(whitelist::add);
                }
            }
        });

        cn.setValue(entityTypeToken, whitelist);
    }
}
