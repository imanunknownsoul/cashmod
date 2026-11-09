package com.cashmod.economy;

import com.cashmod.CashConfig;
import com.cashmod.network.ModNetwork;
import com.cashmod.network.OpenBankPacket;
import com.cashmod.network.SyncBankPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * All the money rules live here (server side only). The GUI and the commands both call these
 * methods, and every check happens here, so nobody can cheat by sending fake requests.
 */
public final class BankManager {
    public static final long MAX_BALANCE = 1_000_000_000_000_000L;
    /** The most inventory stacks of bills we will hand out in one go. */
    public static final int MAX_BILL_STACKS = 36;

    private BankManager() {}

    // ---------- basics ----------

    public static long getBalance(ServerPlayer player) {
        return BankData.get(player.server).getBalance(player.getUUID());
    }

    /** Tells the player's game how much is in their bank so the HUD/screen can show it. */
    public static void sync(ServerPlayer player) {
        ModNetwork.sendToPlayer(player, new SyncBankPacket(getBalance(player)));
    }

    public static void openBankScreen(ServerPlayer player) {
        sync(player);
        ModNetwork.sendToPlayer(player, new OpenBankPacket());
    }

    public static void setBalance(ServerPlayer player, long amount) {
        long clamped = Math.max(0L, Math.min(amount, MAX_BALANCE));
        BankData.get(player.server).setBalance(player.getUUID(), clamped);
        sync(player);
    }

    public static void addToBank(ServerPlayer player, long amount) {
        setBalance(player, getBalance(player) + amount);
    }

    // ---------- deposit / withdraw ----------

    public static void deposit(ServerPlayer player, long amount) {
        if (amount <= 0) {
            error(player, "Enter an amount greater than $0.");
            return;
        }
        long cash = CashUtil.countCash(player);
        if (cash <= 0) {
            error(player, "You don't have any cash on you.");
            return;
        }
        if (amount > cash) {
            error(player, "You only have $" + CashUtil.format(cash) + " in cash - you can't deposit $"
                    + CashUtil.format(amount) + ".");
            return;
        }
        BankData data = BankData.get(player.server);
        long balance = data.getBalance(player.getUUID());
        if (balance + amount > MAX_BALANCE) {
            error(player, "Your bank account can't hold that much.");
            return;
        }

        // Take ALL the player's bills, then give back whatever is left over as change.
        List<ItemStack> change = CashUtil.breakdown(cash - amount);
        if (!CashUtil.hasRoom(player, change, true)) {
            error(player, "Not enough inventory space for your change. Free up a slot and try again.");
            return;
        }
        CashUtil.removeAllCash(player);
        CashUtil.give(player, change);

        data.setBalance(player.getUUID(), balance + amount);
        sync(player);
        ok(player, "Deposited $" + CashUtil.format(amount) + ". Bank balance: $" + CashUtil.format(balance + amount) + ".");
        ding(player);
    }

    public static void withdraw(ServerPlayer player, long amount) {
        if (amount <= 0) {
            error(player, "Enter an amount greater than $0.");
            return;
        }
        BankData data = BankData.get(player.server);
        long balance = data.getBalance(player.getUUID());
        if (amount > balance) {
            error(player, "You only have $" + CashUtil.format(balance) + " in the bank - you can't withdraw $"
                    + CashUtil.format(amount) + ".");
            return;
        }
        if (CashUtil.stackCount(amount) > MAX_BILL_STACKS) {
            long max = (long) MAX_BILL_STACKS * CashUtil.BILL_STACK_SIZE * 100;
            error(player, "That's too many bills to carry at once. Withdraw $" + CashUtil.format(max) + " or less at a time.");
            return;
        }
        List<ItemStack> bills = CashUtil.breakdown(amount);
        if (!CashUtil.hasRoom(player, bills, false)) {
            error(player, "You don't have enough inventory space for that much cash.");
            return;
        }
        data.setBalance(player.getUUID(), balance - amount);
        CashUtil.give(player, bills);
        sync(player);
        ok(player, "Withdrew $" + CashUtil.format(amount) + ". Bank balance: $" + CashUtil.format(balance - amount) + ".");
        ding(player);
    }

    // ---------- paying people ----------

    /**
     * Gives the player money as cash bills. If it won't fit in their inventory it goes into the bank instead.
     * Returns true if it was paid in cash, false if it went to the bank.
     */
    public static boolean payPlayer(ServerPlayer player, long amount) {
        if (amount <= 0) return true;
        if (CashUtil.stackCount(amount) <= MAX_BILL_STACKS) {
            List<ItemStack> bills = CashUtil.breakdown(amount);
            if (CashUtil.hasRoom(player, bills, false)) {
                CashUtil.give(player, bills);
                return true;
            }
        }
        addToBank(player, amount);
        return false;
    }

    /** /pay - send money from your bank to another player's bank. */
    public static void transfer(ServerPlayer from, ServerPlayer to, long amount) {
        if (from.getUUID().equals(to.getUUID())) {
            error(from, "You can't pay yourself.");
            return;
        }
        if (amount <= 0) {
            error(from, "Enter an amount greater than $0.");
            return;
        }
        long fromBalance = getBalance(from);
        if (amount > fromBalance) {
            error(from, "You only have $" + CashUtil.format(fromBalance) + " in the bank.");
            return;
        }
        long toBalance = getBalance(to);
        if (toBalance + amount > MAX_BALANCE) {
            error(from, to.getName().getString() + "'s bank account can't hold that much.");
            return;
        }
        BankData data = BankData.get(from.server);
        data.setBalance(from.getUUID(), fromBalance - amount);
        data.setBalance(to.getUUID(), toBalance + amount);
        sync(from);
        sync(to);
        ok(from, "Sent $" + CashUtil.format(amount) + " from your bank to " + to.getName().getString() + ".");
        tell(to, from.getName().getString() + " sent $" + CashUtil.format(amount) + " to your bank.", ChatFormatting.AQUA);
        ding(to);
    }

    // ---------- hourly pay ----------

    /** Called once per second for every online player. */
    public static void payrollTick(ServerPlayer player) {
        long amount = CashConfig.PAY_AMOUNT.get();
        if (amount <= 0) return;
        long interval = CashConfig.PAY_INTERVAL_MINUTES.get() * 60L * 20L;

        BankData data = BankData.get(player.server);
        int timer = data.getPayTimer(player.getUUID()) + 20;
        if (timer >= interval) {
            timer = 0;
            if (CashConfig.PAY_TO_BANK.get()) {
                addToBank(player, amount);
                ok(player, "[Payday] You earned $" + CashUtil.format(amount) + " - it was added to your bank!");
            } else if (payPlayer(player, amount)) {
                ok(player, "[Payday] You earned $" + CashUtil.format(amount) + " in cash!");
            } else {
                ok(player, "[Payday] You earned $" + CashUtil.format(amount)
                        + " - your inventory was full, so it went to your bank!");
            }
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.2f);
        }
        data.setPayTimer(player.getUUID(), timer);
    }

    public static long secondsUntilPayday(ServerPlayer player) {
        long interval = CashConfig.PAY_INTERVAL_MINUTES.get() * 60L * 20L;
        int timer = BankData.get(player.server).getPayTimer(player.getUUID());
        return Math.max(0L, (interval - timer) / 20L);
    }

    // ---------- chat helpers ----------

    private static void tell(ServerPlayer player, String text, ChatFormatting color) {
        player.displayClientMessage(Component.literal(text).withStyle(color), false);
    }

    public static void error(ServerPlayer player, String text) {
        tell(player, text, ChatFormatting.RED);
    }

    public static void ok(ServerPlayer player, String text) {
        tell(player, text, ChatFormatting.GREEN);
    }

    private static void ding(ServerPlayer player) {
        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.2f);
    }
}
