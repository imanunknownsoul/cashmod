package com.cashmod;

import com.cashmod.network.ModNetwork;
import com.cashmod.registry.ModCreativeTabs;
import com.cashmod.registry.ModItems;
import com.cashmod.registry.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * The main class of the mod. Forge finds it because of the @Mod annotation.
 * The MODID here must match "modId" in META-INF/mods.toml and "mod_id" in gradle.properties.
 */
@Mod(CashMod.MODID)
public class CashMod {
    public static final String MODID = "cashmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CashMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register our items, menus and creative tab with the game.
        ModItems.ITEMS.register(modBus);
        ModMenus.MENUS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        modBus.addListener(this::commonSetup);

        // Config files appear in the "config" folder after the first launch.
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CashConfig.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CashConfig.CLIENT_SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
