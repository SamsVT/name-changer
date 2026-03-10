package com.sammy.namechanger1_20_1.nickname;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.DimensionDataStorage;

import com.sammy.namechanger1_20_1.Namechanger1_20_1;

public final class NicknameService {
    private static final String DATA_NAME = Namechanger1_20_1.MOD_ID + "_nicknames";

    private NicknameService() {
    }

    public static void init() {
        PlayerEvent.PLAYER_JOIN.register(NicknameNetworking::syncToPlayer);
    }

    public static boolean hasNickname(MinecraftServer server, UUID playerId) {
        return getData(server).getNicknames().containsKey(playerId);
    }

    public static String getNickname(MinecraftServer server, UUID playerId) {
        return getData(server).getNicknames().get(playerId);
    }

    public static void setNickname(MinecraftServer server, UUID playerId, String nickname) {
        getData(server).getNicknames().put(playerId, nickname);
        getData(server).setDirty();
    }

    public static void resetNickname(MinecraftServer server, UUID playerId) {
        getData(server).getNicknames().remove(playerId);
        getData(server).setDirty();
    }

    public static boolean isHideAllNames(MinecraftServer server) {
        return getData(server).isHideAllNames();
    }

    public static void setHideAllNames(MinecraftServer server, boolean enabled) {
        getData(server).setHideAllNames(enabled);
    }

    public static Map<UUID, String> getNicknameSnapshot(MinecraftServer server) {
        return new LinkedHashMap<>(getData(server).getNicknames());
    }

    private static NicknameSavedData getData(MinecraftServer server) {
        DimensionDataStorage dataStorage = server.overworld().getDataStorage();
        return dataStorage.computeIfAbsent(NicknameSavedData::load, NicknameSavedData::new, DATA_NAME);
    }
}
