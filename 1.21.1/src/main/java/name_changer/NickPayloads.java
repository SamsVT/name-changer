package name_changer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NickPayloads {
    private NickPayloads() {}

    public static final CustomPayload.Id<SyncAllS2CPayload> S2C_SYNC_ALL =
            new CustomPayload.Id<>(Identifier.of("name_changer", "sync_all"));

    public static final CustomPayload.Id<SetOneNickS2CPayload> S2C_SET_ONE_NICK =
            new CustomPayload.Id<>(Identifier.of("name_changer", "set_one"));

    public static final CustomPayload.Id<HideAllS2CPayload> S2C_HIDE_ALL =
            new CustomPayload.Id<>(Identifier.of("name_changer", "hide_all"));

    public static void register() {
        PayloadTypeRegistry.playS2C().register(S2C_SYNC_ALL, SyncAllS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_SET_ONE_NICK, SetOneNickS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(S2C_HIDE_ALL, HideAllS2CPayload.CODEC);
    }

    private static void writeUuid(RegistryByteBuf buf, UUID id) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }

    private static UUID readUuid(RegistryByteBuf buf) {
        long msb = buf.readLong();
        long lsb = buf.readLong();
        return new UUID(msb, lsb);
    }

    public record SyncAllS2CPayload(Map<UUID, String> nicks, boolean hideAll) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, SyncAllS2CPayload> CODEC = PacketCodec.of(
                (payload, buf) -> {
                    buf.writeBoolean(payload.hideAll);
                    buf.writeVarInt(payload.nicks.size());
                    for (var e : payload.nicks.entrySet()) {
                        writeUuid(buf, e.getKey());
                        buf.writeString(e.getValue(), 64);
                    }
                },
                (buf) -> {
                    boolean hideAll = buf.readBoolean();
                    int size = buf.readVarInt();
                    Map<UUID, String> map = new HashMap<>(Math.max(16, size));
                    for (int i = 0; i < size; i++) {
                        UUID id = readUuid(buf);
                        String nick = buf.readString(64);
                        if (nick != null && !nick.isBlank()) map.put(id, nick);
                    }
                    return new SyncAllS2CPayload(map, hideAll);
                }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return S2C_SYNC_ALL;
        }
    }

    public record SetOneNickS2CPayload(UUID uuid, String nick) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, SetOneNickS2CPayload> CODEC = PacketCodec.of(
                (payload, buf) -> {
                    writeUuid(buf, payload.uuid);
                    buf.writeString(payload.nick == null ? "" : payload.nick, 64);
                },
                (buf) -> {
                    UUID id = readUuid(buf);
                    String nick = buf.readString(64);
                    return new SetOneNickS2CPayload(id, nick);
                }
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return S2C_SET_ONE_NICK;
        }
    }

    public record HideAllS2CPayload(boolean hideAll) implements CustomPayload {
        public static final PacketCodec<RegistryByteBuf, HideAllS2CPayload> CODEC = PacketCodec.of(
                (payload, buf) -> buf.writeBoolean(payload.hideAll),
                (buf) -> new HideAllS2CPayload(buf.readBoolean())
        );

        @Override
        public Id<? extends CustomPayload> getId() {
            return S2C_HIDE_ALL;
        }
    }
}
