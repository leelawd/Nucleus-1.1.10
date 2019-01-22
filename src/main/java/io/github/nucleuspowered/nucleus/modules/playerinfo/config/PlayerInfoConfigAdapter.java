/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.config;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.qsml.NucleusConfigAdapter;

import java.util.List;

public class PlayerInfoConfigAdapter extends NucleusConfigAdapter.StandardWithSimpleDefault<PlayerInfoConfig> {

    @Override protected List<Transformation> getTransformations() {
        return Lists.newArrayList(
            Transformation.moveFrom("list", "group-by-permission-groups").to("list", "list-grouping-by-permission", "enabled"),
            Transformation.moveFrom("list", "default-group-name").to("list", "list-grouping-by-permission", "default-group-name")
        );
    }

    public PlayerInfoConfigAdapter() {
        super(PlayerInfoConfig.class);
    }
}
