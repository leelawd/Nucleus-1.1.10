/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.nucleusdata.WarpCategory;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Lists all the warps that a subject can access.
 */
@Permissions(prefix = "warp", suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand(value = {"list"}, subcommandOf = WarpCommand.class, rootAliasRegister = "warps")
public class ListWarpCommand extends AbstractCommand<CommandSource> {

    @Inject private WarpHandler service;
    @Inject private WarpConfigAdapter adapter;

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("u").buildWith(GenericArguments.none())
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        if (service.getWarpNames().isEmpty()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.list.nowarps"));
            return CommandResult.empty();
        }

        return !args.hasAny("u") && adapter.getNodeOrDefault().isCategoriseWarps() ? categories(src) : noCategories(src);
    }

    private boolean canView(CommandSource src, String warp) {
        return !adapter.getNodeOrDefault().isSeparatePermissions()
                || src.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "warps." + warp.toLowerCase());
    }

    private CommandResult categories(final CommandSource src) {
        // Get the warp list.
        Map<WarpCategory, List<Warp>> warps = service.getWarpsWithCategories(x -> canView(src, x.getName()));
        createMain(src, warps);
        return CommandResult.success();
    }

    private void createMain(final CommandSource src, final Map<WarpCategory, List<Warp>> warps) {
        List<Text> lt = warps.keySet().stream().filter(Objects::nonNull)
                .sorted(Comparator.comparing(WarpCategory::getId))
                .map(s -> {
                    Text.Builder t = Text.builder("> ").color(TextColors.GREEN).style(TextStyles.ITALIC)
                            .append(s.getDisplayName())
                            .onClick(TextActions.executeCallback(source -> createSub(source, s, warps)));
                    s.getDescription().ifPresent(x -> t.append(Text.of(" - ")).append(Text.of(TextColors.RESET, TextStyles.NONE, x)));
                    return t.build();
                })
                .collect(Collectors.toList());

        // Uncategorised
        if (warps.containsKey(null)) {
            lt.add(Text.builder("> " + adapter.getNodeOrDefault().getDefaultName()).color(TextColors.GREEN).style(TextStyles.ITALIC)
                .onClick(TextActions.executeCallback(source -> createSub(source, null, warps))).build());
        }

        MessageProvider messageProvider = plugin.getMessageProvider();
        Util.getPaginationBuilder(src)
            .header(messageProvider.getTextMessageWithFormat("command.warps.list.headercategory"))
            .title(messageProvider.getTextMessageWithFormat("command.warps.list.maincategory")).padding(Text.of(TextColors.GREEN, "-"))
            .contents(lt)
            .sendTo(src);
    }

    private void createSub(final CommandSource src, @Nullable final WarpCategory category, final Map<WarpCategory, List<Warp>> warpDataList) {
        final boolean econExists = plugin.getEconHelper().economyServiceExists();
        final double defaultCost = adapter.getNodeOrDefault().getDefaultWarpCost();
        Text name = category == null ? Text.of(adapter.getNodeOrDefault().getDefaultName()) : category.getDisplayName();

        List<Text> lt = warpDataList.get(category).stream().sorted(Comparator.comparing(Warp::getName))
            .map(s -> createWarp(s, s.getName(), econExists, defaultCost)).collect(Collectors.toList());

        Util.getPaginationBuilder(src)
            .title(plugin.getMessageProvider().getTextMessageWithTextFormat("command.warps.list.category", name)).padding(Text.of(TextColors.GREEN, "-"))
            .contents(lt)
            .footer(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.list.back").toBuilder()
                .onClick(TextActions.executeCallback(s -> createMain(s, warpDataList))).build())
            .sendTo(src);
    }

    private CommandResult noCategories(final CommandSource src) {
        // Get the warp list.
        Set<String> ws = service.getWarpNames();
        final boolean econExists = plugin.getEconHelper().economyServiceExists();
        final double defaultCost = adapter.getNodeOrDefault().getDefaultWarpCost();
        List<Text> lt = ws.stream().filter(s -> canView(src, s.toLowerCase())).sorted(String::compareTo).map(s -> {
            Optional<Warp> wd = service.getWarp(s);
            return createWarp(wd.orElse(null), s, econExists, defaultCost);
        }).collect(Collectors.toList());

        Util.getPaginationBuilder(src)
            .title(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.list.header")).padding(Text.of(TextColors.GREEN, "-"))
            .contents(lt)
            .sendTo(src);

        return CommandResult.success();
    }

    private Text createWarp(@Nullable Warp data, String name, boolean econExists, double defaultCost) {
        if (data == null || !data.getLocation().isPresent()) {
            return Text.builder(name).color(TextColors.RED).onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat
                    ("command.warps.unavailable"))).build();
        }

        Location<World> world = data.getLocation().get();

        Text.Builder inner = Text.builder(name).color(TextColors.GREEN).style(TextStyles.ITALIC)
                .onClick(TextActions.runCommand("/warp \"" + name + "\""));

        Text.Builder tb;
        Optional<Text> description = data.getDescription();
        if (adapter.getNodeOrDefault().isDescriptionInList()) {
            Text.Builder hoverBuilder = Text.builder()
                    .append(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.warpprompt", name))
                    .append(Text.NEW_LINE)
                    .append(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.warplochover", world.getExtent().getName(),
                            world.getBlockPosition().toString()));

            if (econExists) {
                double cost = data.getCost().orElse(defaultCost);
                if (cost > 0) {
                    hoverBuilder
                        .append(Text.NEW_LINE)
                        .append(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.list.costhover", plugin.getEconHelper()
                        .getCurrencySymbol(cost)));
                }
            }

            tb = Text.builder().append(inner.onHover(TextActions.showText(hoverBuilder.build())).build());
            description.ifPresent(text -> tb.append(Text.of(TextColors.WHITE, " - ")).append(text));
        } else {
            if (description.isPresent()) {
                inner.onHover(TextActions.showText(
                        Text.of(
                                plugin.getMessageProvider().getTextMessageWithFormat("command.warps.warpprompt", name),
                                Text.NEW_LINE,
                                description.get()
                        )));
            } else {
                inner.onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.warpprompt", name)));
            }

            tb = Text.builder().append(inner.build())
                            .append(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.warploc",
                                    world.getExtent().getName(), world.getBlockPosition().toString()
                            ));

            if (econExists) {
                double cost = data.getCost().orElse(defaultCost);
                if (cost > 0) {
                    tb.append(plugin.getMessageProvider().getTextMessageWithFormat("command.warps.list.cost", plugin.getEconHelper().getCurrencySymbol(cost)));
                }
            }
        }

        return tb.build();
    }
}
