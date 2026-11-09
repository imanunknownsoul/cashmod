package com.cashmod.event;

import com.cashmod.CashMod;
import com.cashmod.command.ModCommands;
import com.cashmod.economy.BankManager;
import com.cashmod.economy.PriceRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Things that happen "in the world": server start, commands, players joining, hourly pay timer. */
@Mod.EventBusSubscriber(modid = CashMod.MODID)
public class ForgeEvents {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        int count = PriceRegistry.load();
        CashMod.LOGGER.info("Cash Mod loaded {} sell prices", count);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            BankManager.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        // Once per second (every 20 ticks)
        if (player.tickCount % 20 == 0) {
            BankManager.payrollTick(player);
        }
    }
}
