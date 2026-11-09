package com.cashmod.economy;

import com.cashmod.item.BillItem;
import com.cashmod.registry.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Helper methods for counting, removing and handing out cash bills. */
public final class CashUtil {
    /** All bill values, smallest to largest. Add a number here (+ a texture/model/lang entry) for a new bill. */
    public static final int[] DENOMINATIONS = {1, 5, 10, 20, 50, 100};
    public static final int BILL_STACK_SIZE = 64;

    private CashUtil() {}

    /** 1234567 -> "1,234,567" */
    public static String format(long amount) {
        return String.format(Locale.US, "%,d", amount);
    }

    /** Total value of every bill in the player's inventory. */
    public static long countCash(Player player) {
        long total = 0;
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof BillItem bill) {
                total += (long) bill.getValue() * stack.getCount();
            }
        }
        return total;
    }

    /** Deletes every bill from the player's inventory (used together with giving change back). */
    public static void removeAllCash(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() instanceof BillItem) {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /** Splits an amount into the fewest bills, e.g. 135 -> one $100, one $20, one $10, one $5. */
    public static List<ItemStack> breakdown(long amount) {
        List<ItemStack> result = new ArrayList<>();
        long remaining = amount;
        for (int i = DENOMINATIONS.length - 1; i >= 0; i--) {
            int denomination = DENOMINATIONS[i];
            long count = remaining / denomination;
            remaining -= count * denomination;
            while (count > 0) {
                int stackCount = (int) Math.min(count, BILL_STACK_SIZE);
                result.add(new ItemStack(ModItems.bill(denomination), stackCount));
                count -= stackCount;
            }
        }
        return result;
    }

    /** How many inventory stacks that amount of money would need (without creating them all). */
    public static long stackCount(long amount) {
        long stacks = 0;
        long remaining = amount;
        for (int i = DENOMINATIONS.length - 1; i >= 0; i--) {
            int denomination = DENOMINATIONS[i];
            long count = remaining / denomination;
            remaining -= count * denomination;
            stacks += (count + BILL_STACK_SIZE - 1) / BILL_STACK_SIZE;
        }
        return stacks;
    }

    /**
     * Checks (without changing anything) whether these stacks would fit in the player's main inventory.
     * If ignoreBills is true, existing bills are treated as already removed.
     */
    public static boolean hasRoom(Player player, List<ItemStack> incoming, boolean ignoreBills) {
        List<ItemStack> simulated = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) {
            if (ignoreBills && stack.getItem() instanceof BillItem) {
                simulated.add(ItemStack.EMPTY);
            } else {
                simulated.add(stack.copy());
            }
        }
        for (ItemStack in : incoming) {
            int left = in.getCount();
            for (ItemStack existing : simulated) {
                if (left <= 0) break;
                if (!existing.isEmpty() && ItemStack.isSameItemSameTags(existing, in)
                        && existing.getCount() < existing.getMaxStackSize()) {
                    int add = Math.min(left, existing.getMaxStackSize() - existing.getCount());
                    existing.grow(add);
                    left -= add;
                }
            }
            for (int i = 0; i < simulated.size() && left > 0; i++) {
                if (simulated.get(i).isEmpty()) {
                    int put = Math.min(left, in.getMaxStackSize());
                    simulated.set(i, new ItemStack(in.getItem(), put));
                    left -= put;
                }
            }
            if (left > 0) return false;
        }
        return true;
    }

    /** Puts the stacks in the player's inventory (drops any leftovers at their feet). */
    public static void give(Player player, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
        }
    }
}
