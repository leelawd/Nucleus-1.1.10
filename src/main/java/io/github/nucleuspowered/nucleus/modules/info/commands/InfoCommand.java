/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.InfoArgument;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfig;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.info.handlers.InfoHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand({"info", "einfo"})
@EssentialsEquivalent({"info", "ifo", "news", "about", "inform"})
public class InfoCommand extends AbstractCommand<CommandSource> {

    private final InfoHandler infoHandler;
    private final TextParsingUtils textParsingUtils;
    private final InfoConfigAdapter infoConfigAdapter;

    private final String key = "section";

    @Inject
    public InfoCommand(InfoHandler infoHandler, TextParsingUtils textParsingUtils,
            InfoConfigAdapter infoConfigAdapter) {
        this.infoHandler = infoHandler;
        this.textParsingUtils = textParsingUtils;
        this.infoConfigAdapter = infoConfigAdapter;
    }

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> map = new HashMap<>();
        map.put("list", PermissionInformation.getWithTranslation("permission.info.list", SuggestedLevel.ADMIN));
        return map;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().permissionFlag(permissions.getPermissionWithSuffix("list"), "l", "-list").buildWith(
                GenericArguments.optional(new InfoArgument(Text.of(key), infoHandler)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<InfoArgument.Result> oir = args.getOne(key);
        InfoConfig infoConfig = infoConfigAdapter.getNodeOrDefault();
        if (infoConfig.isUseDefaultFile() && !oir.isPresent() && !args.hasAny("l")) {
            // Do we have a default?
            String def = infoConfig.getDefaultInfoSection();
            Optional<TextFileController> list = infoHandler.getSection(def);
            if (list.isPresent()) {
                oir = Optional.of(new InfoArgument.Result(infoHandler.getInfoSections().stream().filter(def::equalsIgnoreCase).findFirst().get(), list.get()));
            }
        }

        if (oir.isPresent()) {
            TextFileController controller = oir.get().text;
            Text def = TextSerializers.FORMATTING_CODE.deserialize(oir.get().name);
            Text title = plugin.getMessageProvider().getTextMessageWithTextFormat("command.info.title.section",
                    controller.getTitle(src).orElseGet(() -> Text.of(def)));

            controller.sendToPlayer(src, title);
            return CommandResult.success();
        }

        // Create a list of pages to load.
        Set<String> sections = infoHandler.getInfoSections();
        if (sections.isEmpty()) {
            throw new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("command.info.none"));
        }

        // Create the text.
        List<Text> s = Lists.newArrayList();
        sections.forEach(x -> {
            Text.Builder tb = Text.builder().append(Text.builder(x)
                    .color(TextColors.GREEN).style(TextStyles.ITALIC)
                    .onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("command.info.hover", x)))
                    .onClick(TextActions.runCommand("/info " + x)).build());

            // If there is a title, then add it.
            infoHandler.getSection(x).get().getTitle(src).ifPresent(sub ->
                tb.append(Text.of(TextColors.GOLD, " - ")).append(sub)
            );

            s.add(tb.build());
        });

        Util.getPaginationBuilder(src).contents()
                .header(plugin.getMessageProvider().getTextMessageWithFormat("command.info.header.default"))
                .title(plugin.getMessageProvider().getTextMessageWithFormat("command.info.title.default"))
                .contents(s.stream().sorted(Comparator.comparing(Text::toPlain)).collect(Collectors.toList()))
                .padding(Text.of(TextColors.GOLD, "-")).sendTo(src);
        return CommandResult.success();
    }
}
