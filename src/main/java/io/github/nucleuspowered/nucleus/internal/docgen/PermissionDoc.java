/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class PermissionDoc {

    @Setting
    private String permission;

    @Setting
    private String description;

    @Setting
    private String defaultLevel;

    @Setting
    private String module;

    private boolean isOre = true;
    private boolean isNormal = true;

    public String getPermission() {
        return permission;
    }

    public PermissionDoc setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public PermissionDoc setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getModule() {
        return module;
    }

    public PermissionDoc setModule(String module) {
        this.module = module;
        return this;
    }

    public String getDefaultLevel() {
        return defaultLevel;
    }

    public PermissionDoc setDefaultLevel(String defaultLevel) {
        this.defaultLevel = defaultLevel;
        return this;
    }

    public boolean isOre() {
        return isOre;
    }

    public boolean isNormal() {
        return isNormal;
    }

    public PermissionDoc setOre(boolean ore) {
        isOre = ore;
        return this;
    }

    public PermissionDoc setNormal(boolean normal) {
        isNormal = normal;
        return this;
    }
}
