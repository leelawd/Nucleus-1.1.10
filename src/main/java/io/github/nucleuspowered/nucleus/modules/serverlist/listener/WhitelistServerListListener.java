/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.serverlist.listener;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.modules.serverlist.ServerListModule;
import io.github.nucleuspowered.nucleus.modules.serverlist.config.ServerListConfig;
import io.github.nucleuspowered.nucleus.modules.serverlist.config.ServerListConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.serverlist.datamodules.ServerListGeneralDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.server.ClientPingServerEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@ConditionalListener(WhitelistServerListListener.Condition.class)
public class WhitelistServerListListener extends ListenerBase.Reloadable {

    private final Random random = new Random();
    private ServerListConfig config;

    @Listener(order = Order.LATE)
    public void onServerListPing(ClientPingServerEvent event, @Getter("getResponse") ClientPingServerEvent.Response response) {
        if (!Sponge.getServer().hasWhitelist()) {
            return;
        }

        Optional<Text> ott = plugin.getGeneralService()
                .quickGet(ServerListGeneralDataModule.class, ServerListGeneralDataModule::getMessage);

        if (!ott.isPresent() &&  !this.config.getWhitelist().isEmpty()) {
            List<NucleusTextTemplateImpl> list = this.config.getWhitelist();

            if (list != null) {
                NucleusTextTemplate template = list.get(this.random.nextInt(list.size()));
                response.setDescription(template.getForCommandSource(Sponge.getServer().getConsole()));
            }
        }
    }

    @Override
    public void onReload() throws Exception {
        this.config = this.plugin.getConfigValue(ServerListModule.ID, ServerListConfigAdapter.class, Function.identity())
                .orElseGet(ServerListConfig::new);
    }

    public static class Condition implements Predicate<Nucleus> {

        @Override
        public boolean test(Nucleus nucleus) {
            return nucleus.getConfigValue(ServerListModule.ID, ServerListConfigAdapter.class, ServerListConfig::enableWhitelistListener).orElse(false);
        }
    }
}
