package name_changer;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NickSync {

    public static final Identifier S2C_SYNC_ALL    = new Identifier("name_changer", "sync_all");
    public static final Identifier S2C_SET_ONE_NICK = new Identifier("name_changer", "set_one_nick");
    public static final Identifier S2C_HIDE_ALL     = new Identifier("name_changer", "hide_all");

    private NickSync() {}

    public static void sendAllTo(ServerPlayerEntity player, Map<UUID, String> nicks, boolean hideAll) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(hideAll);

        buf.writeVarInt(nicks.size());
        for (var e : nicks.entrySet()) {
            buf.writeUuid(e.getKey());
            buf.writeString(e.getValue(), 64);
        }

        ServerPlayNetworking.send(player, S2C_SYNC_ALL, buf);
    }

    public static void broadcastNick(MinecraftServer server, UUID uuid, String nickOrEmpty) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeUuid(uuid);
        buf.writeString(nickOrEmpty == null ? "" : nickOrEmpty, 64);

        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, S2C_SET_ONE_NICK, buf);
        }
    }

    public static void broadcastHideAll(MinecraftServer server, boolean hideAll) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(hideAll);

        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, S2C_HIDE_ALL, buf);
        }
    }

    public static SyncAll readAll(PacketByteBuf buf) {
        boolean hideAll = buf.readBoolean();

        int n = buf.readVarInt();
        Map<UUID, String> nicks = new HashMap<>();
        for (int i = 0; i < n; i++) {
            UUID id = buf.readUuid();
            String nick = buf.readString(64);
            if (nick != null && !nick.isBlank()) nicks.put(id, nick);
        }

        return new SyncAll(nicks, hideAll);
    }

    public record SyncAll(Map<UUID, String> nicks, boolean hideAll) {}
}
