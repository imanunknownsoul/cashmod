package com.cashmod.registry;

import com.cashmod.CashMod;
import com.cashmod.menu.SellMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, CashMod.MODID);

    public static final RegistryObject<MenuType<SellMenu>> SELL_MENU =
            MENUS.register("sell", () -> new MenuType<>(SellMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
