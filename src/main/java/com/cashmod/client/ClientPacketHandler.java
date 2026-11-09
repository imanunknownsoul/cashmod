package com.cashmod.client;

import com.cashmod.client.screen.BankScreen;
import net.minecraft.client.Minecraft;

/** Client-only code that packets are allowed to call. */
public final class ClientPacketHandler {
    private ClientPacketHandler() {}

    public static void openBankScreen() {
        Minecraft.getInstance().setScreen(new BankScreen());
    }
}
