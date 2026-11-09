package com.cashmod.client;

import com.cashmod.CashConfig;
import com.cashmod.economy.CashUtil;
import com.cashmod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/** The little "Cash / Bank" display in the top-left corner. Turn it off in config/cashmod-client.toml. */
public final class CashHud {
    private CashHud() {}

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int width, int height) {
        Minecraft mc = Minecraft.getInstance();
        if (!CashConfig.SHOW_HUD.get() || mc.player == null || mc.options.hideGui || mc.options.renderDebug) {
            return;
        }
        long cash = CashUtil.countCash(mc.player);
        long bank = ClientBankData.getBalance();

        int x = 6;
        int y = 6;
        graphics.renderItem(new ItemStack(ModItems.bill(100)), x, y);
        graphics.drawString(mc.font, "Cash: $" + CashUtil.format(cash), x + 20, y + 4, 0x55FF55, true);
        graphics.renderItem(new ItemStack(ModItems.bill(20)), x, y + 18);
        graphics.drawString(mc.font, "Bank: $" + CashUtil.format(bank), x + 20, y + 22, 0x55FFFF, true);
    }
}
