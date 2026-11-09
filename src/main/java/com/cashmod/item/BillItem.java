package com.cashmod.item;

import com.cashmod.economy.BankData;
import com.cashmod.economy.CashUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** A dollar bill. One class is used for every denomination; only "value" differs. */
public class BillItem extends Item {
    private final int value;

    public BillItem(int value, Properties properties) {
        super(properties);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /** Right-click a bill to see how much money you have (shows above the hotbar). */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            long cash = CashUtil.countCash(sp);
            long bank = BankData.get(sp.server).getBalance(sp.getUUID());
            sp.displayClientMessage(Component.literal(
                    "Cash: $" + CashUtil.format(cash) + "   |   Bank: $" + CashUtil.format(bank))
                    .withStyle(ChatFormatting.GREEN), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Worth $" + value + " each").withStyle(ChatFormatting.GREEN));
        if (stack.getCount() > 1) {
            tooltip.add(Component.literal("Stack total: $" + CashUtil.format((long) value * stack.getCount()))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
