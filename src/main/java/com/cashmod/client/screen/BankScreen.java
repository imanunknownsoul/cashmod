package com.cashmod.client.screen;

import com.cashmod.client.ClientBankData;
import com.cashmod.economy.CashUtil;
import com.cashmod.network.BankActionPacket;
import com.cashmod.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * The /bank screen. Shows your bank balance and cash, with Deposit and Withdraw buttons.
 * Clicking one swaps to an "enter an amount" page.
 */
public class BankScreen extends Screen {
    private enum Mode { MAIN, DEPOSIT, WITHDRAW }

    private static final int W = 220;
    private static final int H = 150;

    private Mode mode = Mode.MAIN;
    private EditBox amountBox;
    private int left;
    private int top;

    public BankScreen() {
        super(Component.literal("Bank"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        if (mode == Mode.MAIN) {
            addRenderableWidget(Button.builder(Component.literal("Deposit"), b -> switchTo(Mode.DEPOSIT))
                    .bounds(left + 15, top + 90, 90, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Withdraw"), b -> switchTo(Mode.WITHDRAW))
                    .bounds(left + W - 105, top + 90, 90, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                    .bounds(left + W / 2 - 35, top + 118, 70, 20).build());
        } else {
            amountBox = new EditBox(font, left + 30, top + 68, W - 60, 20, Component.literal("Amount"));
            amountBox.setMaxLength(15);
            amountBox.setFilter(text -> text.matches("\\d*")); // digits only
            amountBox.setHint(Component.literal("Type an amount...").withStyle(ChatFormatting.DARK_GRAY));
            addRenderableWidget(amountBox);
            setInitialFocus(amountBox);

            addRenderableWidget(Button.builder(Component.literal("Confirm"), b -> submit())
                    .bounds(left + 15, top + 105, 60, 20).build());
            addRenderableWidget(Button.builder(Component.literal("All"), b -> fillAll())
                    .bounds(left + 80, top + 105, 60, 20).build());
            addRenderableWidget(Button.builder(Component.literal("Back"), b -> switchTo(Mode.MAIN))
                    .bounds(left + 145, top + 105, 60, 20).build());
        }
    }

    private void switchTo(Mode newMode) {
        mode = newMode;
        rebuildWidgets();
    }

    private long available() {
        long cash = minecraft != null && minecraft.player != null ? CashUtil.countCash(minecraft.player) : 0;
        return mode == Mode.DEPOSIT ? cash : ClientBankData.getBalance();
    }

    private void fillAll() {
        amountBox.setValue(Long.toString(available()));
    }

    private void submit() {
        String text = amountBox.getValue().trim();
        if (text.isEmpty()) return;
        long amount;
        try {
            amount = Long.parseLong(text);
        } catch (NumberFormatException e) {
            return;
        }
        if (amount <= 0) return;
        // The server double-checks everything and replies in chat.
        ModNetwork.CHANNEL.sendToServer(new BankActionPacket(mode == Mode.DEPOSIT, amount));
        switchTo(Mode.MAIN);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (mode != Mode.MAIN && (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)) {
            submit();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);

        // panel
        graphics.fill(left, top, left + W, top + H, 0xFF8B8B8B);
        graphics.fill(left + 1, top + 1, left + W - 1, top + H - 1, 0xFF1E1E1E);

        int centerX = left + W / 2;
        long cash = minecraft != null && minecraft.player != null ? CashUtil.countCash(minecraft.player) : 0;
        long bank = ClientBankData.getBalance();

        if (mode == Mode.MAIN) {
            graphics.drawCenteredString(font, "BANK", centerX, top + 10, 0xFFFFFF);
            graphics.drawCenteredString(font, "Bank balance: $" + CashUtil.format(bank), centerX, top + 36, 0x55FFFF);
            graphics.drawCenteredString(font, "Cash on you: $" + CashUtil.format(cash), centerX, top + 54, 0x55FF55);
            graphics.drawCenteredString(font, "Choose an option:", centerX, top + 75, 0xAAAAAA);
        } else {
            String verb = mode == Mode.DEPOSIT ? "deposit" : "withdraw";
            graphics.drawCenteredString(font, verb.toUpperCase(), centerX, top + 10, 0xFFFFFF);
            graphics.drawCenteredString(font, "How much would you like to " + verb + "?", centerX, top + 32, 0xFFFFFF);
            graphics.drawCenteredString(font, "Available: $" + CashUtil.format(available()), centerX, top + 50,
                    mode == Mode.DEPOSIT ? 0x55FF55 : 0x55FFFF);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
