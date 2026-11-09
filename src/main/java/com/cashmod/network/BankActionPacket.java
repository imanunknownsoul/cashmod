package com.cashmod.network;

import com.cashmod.economy.BankManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client -> server: "I clicked Deposit/Withdraw in the bank screen and typed this amount".
 * The server re-checks everything, so a hacked client can't cheat.
 */
public record BankActionPacket(boolean deposit, long amount) {

    public static void encode(BankActionPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.deposit);
        buf.writeLong(msg.amount);
    }

    public static BankActionPacket decode(FriendlyByteBuf buf) {
        return new BankActionPacket(buf.readBoolean(), buf.readLong());
    }

    public static void handle(BankActionPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (msg.deposit) {
                BankManager.deposit(player, msg.amount);
            } else {
                BankManager.withdraw(player, msg.amount);
            }
        });
        context.setPacketHandled(true);
    }
}
