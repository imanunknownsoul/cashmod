package com.cashmod.economy;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The starting price list. It is written to config/cashmod-prices.json on first launch,
 * and after that you edit the JSON file instead of this code.
 */
final class DefaultPrices {
    private DefaultPrices() {}

    private static Map<String, Integer> map;

    private static void mc(String path, int price) {
        map.put("minecraft:" + path, price);
    }

    static Map<String, Integer> create() {
        map = new LinkedHashMap<>();

        // Item tags (cover every wood type, color, etc. at once)
        map.put("#minecraft:logs", 3);
        map.put("#minecraft:planks", 1);
        map.put("#minecraft:leaves", 1);
        map.put("#minecraft:saplings", 2);
        map.put("#minecraft:wool", 3);
        map.put("#minecraft:flowers", 2);
        map.put("#minecraft:music_discs", 60);
        map.put("#minecraft:coals", 5);

        // Common blocks
        mc("cobblestone", 1); mc("stone", 1); mc("cobbled_deepslate", 1); mc("deepslate", 1);
        mc("dirt", 1); mc("grass_block", 1); mc("coarse_dirt", 1);
        mc("sand", 1); mc("red_sand", 1); mc("gravel", 1);
        mc("andesite", 1); mc("diorite", 1); mc("granite", 1); mc("tuff", 1);
        mc("netherrack", 1); mc("sandstone", 2); mc("terracotta", 3);
        mc("clay_ball", 2); mc("clay", 8); mc("brick", 3); mc("glass", 2);
        mc("ice", 3); mc("snowball", 1); mc("snow_block", 2);
        mc("soul_sand", 2); mc("end_stone", 3); mc("basalt", 2); mc("blackstone", 2);
        mc("obsidian", 25); mc("crying_obsidian", 40);
        mc("cactus", 2); mc("sponge", 60); mc("moss_block", 3); mc("mycelium", 10); mc("cobweb", 6);
        mc("stick", 1);

        // Farming and food
        mc("wheat", 2); mc("wheat_seeds", 1); mc("beetroot_seeds", 1);
        mc("melon_seeds", 1); mc("pumpkin_seeds", 1);
        mc("carrot", 2); mc("potato", 2); mc("poisonous_potato", 1); mc("baked_potato", 4);
        mc("beetroot", 2); mc("melon_slice", 1); mc("melon", 8);
        mc("pumpkin", 6); mc("carved_pumpkin", 6); mc("hay_block", 18);
        mc("sugar_cane", 2); mc("sugar", 3); mc("bamboo", 1); mc("cocoa_beans", 3);
        mc("sweet_berries", 2); mc("glow_berries", 3); mc("kelp", 1); mc("dried_kelp", 2);
        mc("egg", 2); mc("apple", 4); mc("golden_apple", 150); mc("enchanted_golden_apple", 1500);
        mc("golden_carrot", 40); mc("bread", 8); mc("cookie", 2); mc("cake", 20); mc("pumpkin_pie", 12);
        mc("red_mushroom", 3); mc("brown_mushroom", 3);
        mc("honeycomb", 10); mc("honey_bottle", 12); mc("milk_bucket", 15);
        mc("beef", 4); mc("cooked_beef", 8); mc("porkchop", 4); mc("cooked_porkchop", 8);
        mc("chicken", 3); mc("cooked_chicken", 6); mc("mutton", 4); mc("cooked_mutton", 7);
        mc("rabbit", 4); mc("cooked_rabbit", 7);
        mc("cod", 3); mc("cooked_cod", 5); mc("salmon", 3); mc("cooked_salmon", 5);
        mc("tropical_fish", 6); mc("pufferfish", 6);

        // Mob drops and rare stuff
        mc("rotten_flesh", 1); mc("bone", 3); mc("bone_meal", 1); mc("string", 3);
        mc("spider_eye", 4); mc("gunpowder", 8); mc("arrow", 2); mc("feather", 2);
        mc("leather", 5); mc("rabbit_hide", 3); mc("rabbit_foot", 40);
        mc("ink_sac", 4); mc("glow_ink_sac", 8); mc("slime_ball", 10);
        mc("ender_pearl", 25); mc("ender_eye", 45);
        mc("blaze_rod", 20); mc("blaze_powder", 12); mc("ghast_tear", 50); mc("magma_cream", 15);
        mc("phantom_membrane", 30); mc("prismarine_shard", 5); mc("prismarine_crystals", 8);
        mc("shulker_shell", 100); mc("nautilus_shell", 150); mc("scute", 40);
        mc("heart_of_the_sea", 500); mc("totem_of_undying", 1000); mc("nether_star", 2500);
        mc("dragon_egg", 10000); mc("elytra", 2000); mc("trident", 600);
        mc("echo_shard", 150); mc("disc_fragment_5", 100);
        mc("wither_skeleton_skull", 1500); mc("dragon_head", 3000); mc("beacon", 3000);
        mc("name_tag", 60); mc("saddle", 80); mc("experience_bottle", 50);
        mc("nether_wart", 4); mc("flint", 2);

        // Ores, metals and gems
        mc("coal", 5); mc("charcoal", 4);
        mc("raw_iron", 10); mc("iron_ingot", 15); mc("iron_nugget", 2);
        mc("raw_copper", 4); mc("copper_ingot", 7);
        mc("raw_gold", 22); mc("gold_ingot", 30); mc("gold_nugget", 3);
        mc("diamond", 200); mc("emerald", 100); mc("lapis_lazuli", 6); mc("redstone", 5);
        mc("quartz", 8); mc("amethyst_shard", 10);
        mc("netherite_scrap", 400); mc("netherite_ingot", 2500); mc("ancient_debris", 600);
        mc("glowstone_dust", 5); mc("glowstone", 20);

        // Storage blocks
        mc("coal_block", 45); mc("iron_block", 135); mc("gold_block", 270);
        mc("diamond_block", 1800); mc("emerald_block", 900); mc("lapis_block", 54);
        mc("redstone_block", 45); mc("copper_block", 63); mc("netherite_block", 22500);
        mc("quartz_block", 32); mc("raw_iron_block", 90); mc("raw_gold_block", 198);
        mc("raw_copper_block", 36);

        // Ore blocks
        mc("coal_ore", 6); mc("iron_ore", 12); mc("copper_ore", 6); mc("gold_ore", 24);
        mc("diamond_ore", 210); mc("emerald_ore", 110); mc("lapis_ore", 8); mc("redstone_ore", 6);
        mc("nether_quartz_ore", 9); mc("nether_gold_ore", 6);
        mc("deepslate_coal_ore", 6); mc("deepslate_iron_ore", 12); mc("deepslate_copper_ore", 6);
        mc("deepslate_gold_ore", 24); mc("deepslate_diamond_ore", 210);
        mc("deepslate_emerald_ore", 110); mc("deepslate_lapis_ore", 8);
        mc("deepslate_redstone_ore", 6);

        Map<String, Integer> result = map;
        map = null;
        return result;
    }
}
