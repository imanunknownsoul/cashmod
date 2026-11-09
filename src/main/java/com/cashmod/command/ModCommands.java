package com.cashmod.command;

import com.cashmod.economy.BankManager;
import com.cashmod.economy.CashUtil;
import com.cashmod.economy.PriceRegistry;
import com.cashmod.menu.SellMenu;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkHooks;

public final class ModCommands {
    private ModCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        // /sell - opens the sell window
        dispatcher.register(Commands.literal("sell").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            NetworkHooks.openScreen(player, new SimpleMenuProvider(
                    (id, inventory, p) -> new SellMenu(id, inventory), Component.literal("Sell Items")));
            return 1;
        }));

        // /bank - opens the bank screen
        dispatcher.register(Commands.literal("bank").executes(ctx -> {
            BankManager.openBankScreen(ctx.getSource().getPlayerOrException());
            return 1;
        }));

        // /bankdeposit <amount|all>
        dispatcher.register(Commands.literal("bankdeposit")
                .then(Commands.literal("all").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BankManager.deposit(player, CashUtil.countCash(player));
                    return 1;
                }))
                .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BankManager.deposit(player, LongArgumentType.getLong(ctx, "amount"));
                    return 1;
                })));

        // /bankwithdraw <amount|all>
        dispatcher.register(Commands.literal("bankwithdraw")
                .then(Commands.literal("all").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BankManager.withdraw(player, BankManager.getBalance(player));
                    return 1;
                }))
                .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    BankManager.withdraw(player, LongArgumentType.getLong(ctx, "amount"));
                    return 1;
                })));

        // /balance - quick look at your money in chat
        dispatcher.register(Commands.literal("balance").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            long cash = CashUtil.countCash(player);
            long bank = BankManager.getBalance(player);
            ctx.getSource().sendSuccess(() -> Component.literal("Cash: $" + CashUtil.format(cash)
                    + "   |   Bank: $" + CashUtil.format(bank)).withStyle(ChatFormatting.GREEN), false);
            return 1;
        }));

        // /pay <player> <amount> - send money from your bank to someone else's bank
        dispatcher.register(Commands.literal("pay")
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1)).executes(ctx -> {
                            ServerPlayer from = ctx.getSource().getPlayerOrException();
                            ServerPlayer to = EntityArgument.getPlayer(ctx, "player");
                            BankManager.transfer(from, to, LongArgumentType.getLong(ctx, "amount"));
                            return 1;
                        }))));

        // /payday - how long until your next hourly pay
        dispatcher.register(Commands.literal("payday").executes(ctx -> {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            long seconds = BankManager.secondsUntilPayday(player);
            ctx.getSource().sendSuccess(() -> Component.literal("Next payday in " + (seconds / 60) + "m "
                    + (seconds % 60) + "s of play time.").withStyle(ChatFormatting.YELLOW), false);
            return 1;
        }));

        // Admin commands (need operator / permission level 2)
        dispatcher.register(Commands.literal("bankadmin")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0, BankManager.MAX_BALANCE))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            BankManager.setBalance(target, amount);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Set "
                                                    + target.getName().getString() + "'s bank to $"
                                                    + CashUtil.format(amount)), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("add")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1, BankManager.MAX_BALANCE))
                                        .executes(ctx -> {
                                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                            long amount = LongArgumentType.getLong(ctx, "amount");
                                            BankManager.addToBank(target, amount);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Added $"
                                                    + CashUtil.format(amount) + " to "
                                                    + target.getName().getString() + "'s bank"), true);
                                            return 1;
                                        }))))
                .then(Commands.literal("reloadprices").executes(ctx -> {
                    int count = PriceRegistry.load();
                    ctx.getSource().sendSuccess(() -> Component.literal("Reloaded " + count
                            + " sell prices from config/cashmod-prices.json"), true);
                    return 1;
                })));
    }
}
