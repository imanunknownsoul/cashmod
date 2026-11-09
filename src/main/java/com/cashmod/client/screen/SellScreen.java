package com.cashmod.client.screen;

import com.cashmod.economy.CashUtil;
import com.cashmod.menu.SellMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** What you see when you type /sell. Drawn with code, so no extra GUI texture file is needed. */
public class SellScreen extends AbstractContainerScreen<SellMenu> {

    public SellScreen(SellMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.titleLabelY = 6;
        this.inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Confirm Sale"),
                        button -> press(SellMenu.BUTTON_CONFIRM))
                .bounds(leftPos + 14, topPos + 56, 70, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"),
                        button -> press(SellMenu.BUTTON_CANCEL))
                .bounds(leftPos + 92, topPos + 56, 70, 20).build());
    }

    private void press(int buttonId) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        // window background with a simple bevel
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFC6C6C6);
        graphics.fill(x + 1, y + 1, x + imageWidth - 2, y + 2, 0xFFFFFFFF);
        graphics.fill(x + 1, y + 1, x + 2, y + imageHeight - 2, 0xFFFFFFFF);
        graphics.fill(x + imageWidth - 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        graphics.fill(x + 2, y + imageHeight - 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);

        for (Slot slot : menu.slots) {
            drawSlot(graphics, x + slot.x - 1, y + slot.y - 1);
        }
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        graphics.fill(x, y, x + 17, y + 1, 0xFF373737);
        graphics.fill(x, y, x + 1, y + 17, 0xFF373737);
        graphics.fill(x + 1, y + 17, x + 18, y + 18, 0xFFFFFFFF);
        graphics.fill(x + 17, y + 1, x + 18, y + 18, 0xFFFFFFFF);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        ItemStack stack = menu.getSellStack();
        String text;
        int color;
        if (stack.isEmpty()) {
            text = "Drag an item into the slot";
            color = 0x555555;
        } else if (menu.getValue() <= 0) {
            text = "This item can't be sold";
            color = 0xAA0000;
        } else {
            text = "Worth: $" + CashUtil.format(menu.getValue());
            color = 0x00AA00;
        }
        graphics.drawCenteredString(font, text, imageWidth / 2, 44, color);
    }
}
