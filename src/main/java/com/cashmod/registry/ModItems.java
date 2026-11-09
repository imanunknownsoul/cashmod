package com.cashmod.registry;

import com.cashmod.CashMod;
import com.cashmod.economy.CashUtil;
import com.cashmod.item.BillItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CashMod.MODID);

    /** denomination -> item, e.g. 20 -> the $20 bill. Registered as bill_1, bill_5, bill_10 ... */
    public static final Map<Integer, RegistryObject<Item>> BILLS = new LinkedHashMap<>();

    static {
        for (int denomination : CashUtil.DENOMINATIONS) {
            BILLS.put(denomination, ITEMS.register("bill_" + denomination,
                    () -> new BillItem(denomination, new Item.Properties().stacksTo(CashUtil.BILL_STACK_SIZE))));
        }
    }

    public static Item bill(int denomination) {
        return BILLS.get(denomination).get();
    }
}
