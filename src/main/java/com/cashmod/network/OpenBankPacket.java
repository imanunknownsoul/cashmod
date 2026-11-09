package com.cashmod.network;

import com.cashmod.client.ClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server -> client: "open the bank screen". Sent when you type /bank. */
public record OpenBankPacket() {

    public static void encode(OpenBankPacket msg, FriendlyByteBuf buf) {
    }

    public static OpenBankPacket decode(FriendlyByteBuf buf) {
        return new OpenBankPacket();
    }

    public static void handle(OpenBankPacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.openBankScreen()));
        context.setPacketHandled(true);
    }
}
