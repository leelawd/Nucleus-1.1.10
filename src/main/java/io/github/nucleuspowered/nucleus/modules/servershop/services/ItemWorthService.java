/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.servershop.services;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.service.NucleusServerShopService;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@NonnullByDefault
public class ItemWorthService implements NucleusServerShopService {

    private final NucleusPlugin plugin;

    public ItemWorthService(NucleusPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public Map<CatalogType, Double> getBuyPrices() {
        return plugin.getItemDataService().getServerBuyPrices();
    }

    @Override public Map<CatalogType, Double> getSellPrices() {
        return plugin.getItemDataService().getServerSellPrices();
    }

    @Override public Optional<Double> getBuyPrice(BlockState type) throws NucleusException {
        Preconditions.checkNotNull(type);
        return getFromBlockState(type, x -> getBuyPrice(x.createSnapshot()));
    }

    @Override public Optional<Double> getBuyPrice(ItemStackSnapshot itemStackSnapshot) {
        Preconditions.checkNotNull(itemStackSnapshot);
        double price = plugin.getItemDataService().getDataForItem(itemStackSnapshot).getServerBuyPrice();
        if (price < 0) {
            return Optional.empty();
        }

        return Optional.of(price);
    }

    @Override public Optional<Double> getSellPrice(BlockState type) throws NucleusException {
        Preconditions.checkNotNull(type);
        return getFromBlockState(type, x -> getSellPrice(x.createSnapshot()));
    }

    @Override public Optional<Double> getSellPrice(ItemStackSnapshot itemStackSnapshot) {
        Preconditions.checkNotNull(itemStackSnapshot);
        double price = plugin.getItemDataService().getDataForItem(itemStackSnapshot).getServerSellPrice();
        if (price < 0) {
            return Optional.empty();
        }

        return Optional.of(price);
    }

    private Tuple<Optional<BlockState>, ItemType> getType(DataHolder stack, ItemType type) {
        return Tuple.of(stack.get(Keys.ITEM_BLOCKSTATE), type);
    }

    private Optional<Double> getFromBlockState(BlockState type, Function<ItemStack, Optional<Double>> transform) throws NucleusException {
        return transform.apply(type.getType().getItem().map(x -> {
            ItemStack stack = ItemStack.of(x, 1);
            if (stack.offer(Keys.ITEM_BLOCKSTATE, type).isSuccessful()) {
                return stack;
            }

            return null;
        }).orElseThrow(() ->
            new NucleusException(
                    Text.of("That BlockState does not map to an item!"), NucleusException.ExceptionType.DOES_NOT_EXIST)));
    }
}
