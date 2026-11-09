package com.cashmod.network;

import com.cashmod.client.ClientBankData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: "your bank balance is now X". */
public record SyncBankPacket(long balance) {

    public static void encode(SyncBankPacket msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.balance);
    }

    public static SyncBankPacket decode(FriendlyByteBuf buf) {
        return new SyncBankPacket(buf.readLong());
    }

    public static void handle(SyncBankPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientBankData.setBalance(msg.balance)));
        context.setPacketHandled(true);
    }
}
