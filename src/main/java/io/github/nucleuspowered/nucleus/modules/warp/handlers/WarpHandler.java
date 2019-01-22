/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.handlers;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.nucleusdata.WarpCategory;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.modules.warp.datamodules.WarpGeneralDataModule;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@NonnullByDefault
public class WarpHandler implements NucleusWarpService {

    @Inject private ModularGeneralService store;

    private WarpGeneralDataModule getModule() {
        return store.get(WarpGeneralDataModule.class);
    }

    @Override
    public Optional<Warp> getWarp(String warpName) {
        return getModule().getWarpLocation(warpName);
    }

    @Override
    public boolean removeWarp(String warpName) {
        return getModule().removeWarp(warpName);
    }

    @Override
    public boolean setWarp(String warpName, Location<World> location, Vector3d rotation) {
        return getModule().addWarp(warpName, location, rotation);
    }

    @Override public List<Warp> getAllWarps() {
        return new ArrayList<>(getModule().getWarps().values());
    }

    @Override
    public List<Warp> getUncategorisedWarps() {
        return getWarpsForCategory(x -> !x.getCategory().isPresent());
    }

    @Override
    public List<Warp> getWarpsForCategory(String category) {
        return getWarpsForCategory(x -> x.getCategory().isPresent() && x.getCategory().get().equalsIgnoreCase(category));
    }

    @Override
    public Map<WarpCategory, List<Warp>> getWarpsWithCategories(Predicate<Warp> warpDataPredicate) {
        Preconditions.checkNotNull(warpDataPredicate);
        Map<String, List<Warp>> map = getModule().getWarps().values().stream()
            .filter(warpDataPredicate)
            .collect(Collectors.groupingBy(x -> x.getCategory().orElse("")));
        if (map.containsKey("")) {
            map.put(null, map.get(""));
            map.remove("");
        }

        return map.entrySet().stream().collect(Collectors.toMap(x -> x.getKey() == null ? null :
                        getModule().getWarpCategoryOrDefault(x.getKey()), Map.Entry::getValue));
    }


    @Override
    public boolean removeWarpCost(String warpName) {
        return getModule().setWarpCost(warpName, -1);
    }

    @Override
    public boolean setWarpCost(String warpName, double cost) {
        return getModule().setWarpCost(warpName, cost);
    }

    @Override
    public boolean setWarpCategory(String warpName, @Nullable String category) {
        return getModule().setWarpsWarpCategory(warpName, category);
    }

    @Override
    public boolean setWarpDescription(String warpName, @Nullable Text description) {
        return getModule().setWarpDescription(warpName, description);
    }

    @Override
    public Set<String> getWarpNames() {
        return getModule().getWarps().keySet();
    }

    public WarpCategory getWarpCategoryOrDefault(String category) {
        return getModule().getWarpCategoryOrDefault(category);
    }

    @Override public Optional<WarpCategory> getWarpCategory(String category) {
        return getModule().getWarpCategory(category);
    }

    @Override public boolean setWarpCategoryDisplayName(String category, @Nullable Text displayName) {
        Optional<WarpCategory> cat = getWarpCategory(category);
        if (!cat.isPresent()) {
            return false;
        }

        getModule().updateOrSetWarpCategory(category, displayName, cat.get().getDescription().orElse(null));
        return true;
    }

    @Override public boolean setWarpCategoryDescription(String category, @Nullable Text description) {
        Optional<WarpCategory> cat = getWarpCategory(category);
        if (!cat.isPresent()) {
            return false;
        }

        getModule().updateOrSetWarpCategory(category, cat.get().getDisplayName(), description);
        return true;
    }

    private List<Warp> getWarpsForCategory(Predicate<Warp> filter) {
        return getModule().getWarps().values().stream().filter(filter).collect(Collectors.toList());
    }
}
