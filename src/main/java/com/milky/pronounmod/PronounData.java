package com.milky.pronounmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

public class PronounData extends SavedData {

    private final Map<UUID, String> pronouns = new HashMap<>();

    public static final SavedData.Factory<PronounData> FACTORY =
        new SavedData.Factory<>(PronounData::new, PronounData::load, null);

    public static PronounData get(MinecraftServer server) {
        return server.overworld()
            .getDataStorage()
            .computeIfAbsent(FACTORY, "pronounmod_pronouns");
    }

    public void setPronouns(UUID player, String value) {
        pronouns.put(player, value);
        setDirty();
    }

    public String getPronouns(UUID player) {
        return pronouns.get(player);
    }

    public void clearPronouns(UUID player) {
        pronouns.remove(player);
        setDirty();
    }

    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        for (Map.Entry<UUID, String> entry : pronouns.entrySet()) {
            tag.putString(entry.getKey().toString(), entry.getValue());
        }

        return tag;
    }

    public static PronounData load(
        CompoundTag tag,
        HolderLookup.Provider registries
    ) {
        PronounData data = new PronounData();

        for (String key : tag.getAllKeys()) {
            UUID uuid = UUID.fromString(key);
            String pronouns = tag.getString(key);

            data.pronouns.put(uuid, pronouns);
        }

        return data;
    }
}
