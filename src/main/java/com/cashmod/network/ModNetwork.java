package com.cashmod.network;

import com.cashmod.CashMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/** The "phone line" between the client (your game) and the server. */
public final class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CashMod.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private ModNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, SyncBankPacket.class,
                SyncBankPacket::encode, SyncBankPacket::decode, SyncBankPacket::handle);
        CHANNEL.registerMessage(id++, OpenBankPacket.class,
                OpenBankPacket::encode, OpenBankPacket::decode, OpenBankPacket::handle);
        CHANNEL.registerMessage(id++, BankActionPacket.class,
                BankActionPacket::encode, BankActionPacket::decode, BankActionPacket::handle);
    }

    public static void sendToPlayer(ServerPlayer player, Object message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}
