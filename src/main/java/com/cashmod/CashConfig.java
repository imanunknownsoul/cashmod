package com.cashmod;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Settings you can change without touching code.
 * They show up in the "config" folder as cashmod-common.toml and cashmod-client.toml.
 */
public class CashConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ForgeConfigSpec.LongValue PAY_AMOUNT;
    public static final ForgeConfigSpec.IntValue PAY_INTERVAL_MINUTES;
    public static final ForgeConfigSpec.BooleanValue PAY_TO_BANK;

    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec.BooleanValue SHOW_HUD;

    static {
        ForgeConfigSpec.Builder common = new ForgeConfigSpec.Builder();
        common.push("payroll");
        PAY_AMOUNT = common
                .comment("Dollars paid to each player every pay interval (set to 0 to turn payroll off).")
                .defineInRange("payAmount", 500L, 0L, 1_000_000_000L);
        PAY_INTERVAL_MINUTES = common
                .comment("Minutes of online play time between paydays. 60 = hourly.")
                .defineInRange("payIntervalMinutes", 60, 1, 100_000);
        PAY_TO_BANK = common
                .comment("true = pay goes straight into the bank. false = pay is given as cash bills (goes to the bank if your inventory is full).")
                .define("payToBank", true);
        common.pop();
        COMMON_SPEC = common.build();

        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();
        SHOW_HUD = client
                .comment("Show your cash and bank balance in the top-left corner of the screen.")
                .define("showHud", true);
        CLIENT_SPEC = client.build();
    }
}
