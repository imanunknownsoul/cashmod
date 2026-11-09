package com.cashmod.registry;

import com.cashmod.CashMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CashMod.MODID);

    public static final RegistryObject<CreativeModeTab> CASH_TAB = TABS.register("cash_tab", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.cashmod"))
                    .icon(() -> new ItemStack(ModItems.bill(100)))
                    .displayItems((parameters, output) -> {
                        for (var bill : ModItems.BILLS.values()) {
                            output.accept(bill.get());
                        }
                    })
                    .build());
}
