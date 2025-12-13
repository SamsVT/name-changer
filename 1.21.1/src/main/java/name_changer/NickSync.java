package name_changer;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NickSync {
    private NickSync() {}

    public static void sendAllTo(ServerPlayerEntity player, Map<UUID, String> nicks, boolean hideAll) {
        ServerPlayNetworking.send(player, new NickPayloads.SyncAllS2CPayload(new HashMap<>(nicks), hideAll));
    }

    public static void broadcastNick(MinecraftServer server, UUID uuid, String nickOrEmpty) {
        var payload = new NickPayloads.SetOneNickS2CPayload(uuid, nickOrEmpty == null ? "" : nickOrEmpty);
        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, payload);
        }
    }

    public static void broadcastHideAll(MinecraftServer server, boolean hideAll) {
        var payload = new NickPayloads.HideAllS2CPayload(hideAll);
        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, payload);
        }
    }
}
