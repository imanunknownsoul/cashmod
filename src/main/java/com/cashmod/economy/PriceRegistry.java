package com.cashmod.economy;

import com.cashmod.CashMod;
import com.cashmod.item.BillItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Knows what every item is worth when you /sell it.
 * Prices come from config/cashmod-prices.json (created with defaults the first time the game runs).
 * Keys are item ids ("minecraft:diamond") or item tags starting with # ("#minecraft:logs").
 */
public final class PriceRegistry {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_UNIT_PRICE = 10_000_000;

    private static final Map<ResourceLocation, Integer> ITEM_PRICES = new HashMap<>();
    private static final Map<TagKey<Item>, Integer> TAG_PRICES = new LinkedHashMap<>();

    private PriceRegistry() {}

    public static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("cashmod-prices.json");
    }

    /** Reads the prices file (making it first if it doesn't exist). Returns how many entries were loaded. */
    public static int load() {
        Path file = file();
        Map<String, Integer> raw = new LinkedHashMap<>();
        try {
            if (Files.exists(file)) {
                JsonObject json = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    try {
                        raw.put(entry.getKey(), entry.getValue().getAsInt());
                    } catch (RuntimeException ex) {
                        CashMod.LOGGER.warn("Ignoring bad price for '{}' in {}", entry.getKey(), file.getFileName());
                    }
                }
            } else {
                raw.putAll(DefaultPrices.create());
                Files.createDirectories(file.getParent());
                Files.writeString(file, GSON.toJson(raw));
            }
        } catch (IOException | RuntimeException ex) {
            CashMod.LOGGER.error("Could not read {} - using default prices instead.", file.getFileName(), ex);
            raw.clear();
            raw.putAll(DefaultPrices.create());
        }

        ITEM_PRICES.clear();
        TAG_PRICES.clear();
        for (Map.Entry<String, Integer> entry : raw.entrySet()) {
            int price = Math.max(0, Math.min(entry.getValue(), MAX_UNIT_PRICE));
            String key = entry.getKey().trim();
            if (key.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(key.substring(1));
                if (id != null) TAG_PRICES.put(TagKey.create(Registries.ITEM, id), price);
            } else {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null) ITEM_PRICES.put(id, price);
            }
        }
        return ITEM_PRICES.size() + TAG_PRICES.size();
    }

    /** Price of ONE of this item (0 = can't be sold). */
    public static int unitPrice(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof BillItem) return 0;
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        Integer price = ITEM_PRICES.get(id);
        if (price != null) return price;
        for (Map.Entry<TagKey<Item>, Integer> entry : TAG_PRICES.entrySet()) {
            if (stack.is(entry.getKey())) return entry.getValue();
        }
        return 0;
    }

    /** Price of the whole stack. */
    public static long totalValue(ItemStack stack) {
        return (long) unitPrice(stack) * stack.getCount();
    }
}
