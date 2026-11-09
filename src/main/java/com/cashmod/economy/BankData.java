package com.cashmod.economy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Everyone's bank balance and payday timer, saved inside the world folder
 * (world/data/cashmod_bank.dat) so it survives restarts.
 */
public class BankData extends SavedData {
    private static final String NAME = "cashmod_bank";

    private final Map<UUID, Long> balances = new HashMap<>();
    private final Map<UUID, Integer> payTimers = new HashMap<>();

    public static BankData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(BankData::load, BankData::new, NAME);
    }

    public long getBalance(UUID id) {
        return balances.getOrDefault(id, 0L);
    }

    public void setBalance(UUID id, long amount) {
        balances.put(id, amount);
        setDirty();
    }

    /** Ticks of online time since this player's last payday. */
    public int getPayTimer(UUID id) {
        return payTimers.getOrDefault(id, 0);
    }

    public void setPayTimer(UUID id, int ticks) {
        payTimers.put(id, ticks);
        setDirty();
    }

    public static BankData load(CompoundTag tag) {
        BankData data = new BankData();
        ListTag list = tag.getList("players", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            UUID id = entry.getUUID("id");
            data.balances.put(id, entry.getLong("balance"));
            data.payTimers.put(id, entry.getInt("payTimer"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        Set<UUID> ids = new HashSet<>(balances.keySet());
        ids.addAll(payTimers.keySet());
        for (UUID id : ids) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", id);
            entry.putLong("balance", getBalance(id));
            entry.putInt("payTimer", getPayTimer(id));
            list.add(entry);
        }
        tag.put("players", list);
        return tag;
    }
}
