package com.cashmod.menu;

import com.cashmod.economy.BankManager;
import com.cashmod.economy.CashUtil;
import com.cashmod.economy.PriceRegistry;
import com.cashmod.registry.ModMenus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The /sell window: one slot to drop an item into, plus your inventory.
 * Button 0 = Confirm Sale, button 1 = Cancel (the item is handed back when the window closes).
 */
public class SellMenu extends AbstractContainerMenu {
    public static final int BUTTON_CONFIRM = 0;
    public static final int BUTTON_CANCEL = 1;

    private final Player player;
    private final Container sellContainer = new SimpleContainer(1) {
        @Override
        public void setChanged() {
            super.setChanged();
            SellMenu.this.slotsChanged(this);
        }
    };
    /** Sends the item's value to the client. Menu data is sent as 16-bit numbers, so we split it in two. */
    private final ContainerData data = new SimpleContainerData(2);
    private ItemStack lastAnnounced = ItemStack.EMPTY;

    public SellMenu(int containerId, Inventory inventory) {
        super(ModMenus.SELL_MENU.get(), containerId);
        this.player = inventory.player;

        addSlot(new Slot(sellContainer, 0, 80, 22));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 98 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 156));
        }
        addDataSlots(data);
    }

    /** Value of the item in the sell slot (works on both client and server). */
    public int getValue() {
        return ((data.get(1) & 0xFFFF) << 16) | (data.get(0) & 0xFFFF);
    }

    public ItemStack getSellStack() {
        return this.slots.get(0).getItem();
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container != sellContainer || player.level().isClientSide) return;

        ItemStack stack = sellContainer.getItem(0);
        long value = PriceRegistry.totalValue(stack);
        int clamped = (int) Math.min(value, Integer.MAX_VALUE);
        data.set(0, clamped & 0xFFFF);
        data.set(1, (clamped >>> 16) & 0xFFFF);

        // Tell the player what it's worth (only when the item in the slot actually changes).
        if (!ItemStack.matches(stack, lastAnnounced)) {
            lastAnnounced = stack.copy();
            if (!stack.isEmpty()) {
                if (value > 0) {
                    player.displayClientMessage(Component.literal("That's worth $" + CashUtil.format(value)
                            + ". Click Confirm Sale to sell it, or Cancel to take it back.")
                            .withStyle(ChatFormatting.YELLOW), false);
                } else {
                    player.displayClientMessage(Component.literal("That item can't be sold.")
                            .withStyle(ChatFormatting.RED), false);
                }
            }
        }
    }

    @Override
    public boolean clickMenuButton(Player clicker, int id) {
        if (id == BUTTON_CONFIRM) {
            confirmSale(clicker);
            return true;
        }
        if (id == BUTTON_CANCEL) {
            clicker.closeContainer(); // removed() below gives the item back
            return true;
        }
        return false;
    }

    private void confirmSale(Player clicker) {
        if (!(clicker instanceof ServerPlayer sp)) return;
        ItemStack stack = sellContainer.getItem(0);
        if (stack.isEmpty()) {
            BankManager.error(sp, "Put an item in the slot first.");
            return;
        }
        long value = PriceRegistry.totalValue(stack);
        if (value <= 0) {
            BankManager.error(sp, "That item can't be sold.");
            return;
        }
        String name = stack.getCount() + "x " + stack.getHoverName().getString();
        sellContainer.setItem(0, ItemStack.EMPTY);
        lastAnnounced = ItemStack.EMPTY;

        boolean paidInCash = BankManager.payPlayer(sp, value);
        BankManager.ok(sp, "Sold " + name + " for $" + CashUtil.format(value)
                + (paidInCash ? "." : " (paid into your bank because it wouldn't fit in your inventory)."));
    }

    @Override
    public ItemStack quickMoveStack(Player clicker, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index == 0) {
                // sell slot -> player inventory
                if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
            } else {
                // player inventory -> sell slot
                if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player clicker) {
        return true;
    }

    /** Closing the window (Esc, E, or Cancel) hands back anything still in the sell slot. */
    @Override
    public void removed(Player closer) {
        super.removed(closer);
        this.clearContainer(closer, sellContainer);
    }
}
