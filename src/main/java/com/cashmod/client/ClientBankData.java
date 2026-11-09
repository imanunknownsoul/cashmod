package com.cashmod.client;

/** The bank balance as last reported by the server (used by the HUD and the bank screen). */
public final class ClientBankData {
    private static long balance = 0;

    private ClientBankData() {}

    public static long getBalance() {
        return balance;
    }

    public static void setBalance(long newBalance) {
        balance = newBalance;
    }
}
