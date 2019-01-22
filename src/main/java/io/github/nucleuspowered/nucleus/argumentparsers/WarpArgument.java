/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Returns a {@link Warp}
 */
@NonnullByDefault
@SuppressWarnings("all")
public class WarpArgument extends CommandElement {

    private NucleusWarpService service;
    private final WarpConfigAdapter configAdapter;
    private final boolean permissionCheck;
    private final boolean requiresLocation;

    public WarpArgument(@Nullable Text key, WarpConfigAdapter configAdapter, boolean permissionCheck) {
        this(key, configAdapter, permissionCheck, true);
    }

    public WarpArgument(@Nullable Text key, WarpConfigAdapter configAdapter, boolean permissionCheck, boolean requiresLocation) {
        super(key);
        this.configAdapter = configAdapter;
        this.permissionCheck = permissionCheck;
        this.requiresLocation = requiresLocation;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        getService();

        String warpName = args.next();
        String warp = warpName.toLowerCase();
        if (!service.warpExists(warp)) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.noexist"));
        }

        if (!checkPermission(source, warpName) && !checkPermission(source, warpName.toLowerCase())) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.noperms"));
        }

        return service.getWarp(warpName).orElseThrow(() -> args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.notavailable")));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        getService();

        try {
            String name = args.peek().toLowerCase();
            return service.getWarpNames().stream().filter(s -> s.startsWith(name))
                .filter(s -> !requiresLocation || service.getWarp(s).get().getLocation().isPresent())
                .filter(x -> checkPermission(src, name)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, String name) {
        if (!permissionCheck || !configAdapter.getNodeOrDefault().isSeparatePermissions()) {
            return true;
        }

        // No permissions, no entry!
        return src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "warps." + name.toLowerCase());
    }

    private void getService() {
        if (service == null) {
            service = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);
        }
    }
}
