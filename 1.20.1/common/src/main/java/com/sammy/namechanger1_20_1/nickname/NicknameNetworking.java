package com.sammy.namechanger1_20_1.nickname;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import com.sammy.namechanger1_20_1.Namechanger1_20_1;
import com.sammy.namechanger1_20_1.command.NickCommand;

public final class NicknameNetworking {
    public static final ResourceLocation SYNC_STATE_PACKET = new ResourceLocation(Namechanger1_20_1.MOD_ID, "sync_state");

    private NicknameNetworking() {
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    public static void syncToPlayer(ServerPlayer player) {
        if (!NetworkManager.canPlayerReceive(player, SYNC_STATE_PACKET)) {
            return;
        }

        SyncState syncState = createSyncState(player.server);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        writeSyncState(buffer, syncState);
        NetworkManager.sendToPlayer(player, SYNC_STATE_PACKET, buffer);
    }

    public static SyncState createSyncState(MinecraftServer server) {
        return new SyncState(NicknameService.isHideAllNames(server), NicknameService.getNicknameSnapshot(server));
    }

    public static void writeSyncState(FriendlyByteBuf buffer, SyncState syncState) {
        buffer.writeBoolean(syncState.hideAllNames());
        buffer.writeVarInt(syncState.nicknames().size());

        for (Map.Entry<UUID, String> entry : syncState.nicknames().entrySet()) {
            buffer.writeUUID(entry.getKey());
            buffer.writeUtf(entry.getValue(), NickCommand.MAX_NICKNAME_LENGTH);
        }
    }

    public static SyncState readSyncState(FriendlyByteBuf buffer) {
        boolean hideAllNames = buffer.readBoolean();
        int nicknameCount = buffer.readVarInt();
        Map<UUID, String> nicknames = new LinkedHashMap<>();

        for (int index = 0; index < nicknameCount; index++) {
            nicknames.put(buffer.readUUID(), buffer.readUtf(NickCommand.MAX_NICKNAME_LENGTH));
        }

        return new SyncState(hideAllNames, nicknames);
    }

    public record SyncState(boolean hideAllNames, Map<UUID, String> nicknames) {
    }
}
