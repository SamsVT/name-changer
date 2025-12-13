package name_changer;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NickState extends PersistentState {

    private final Map<UUID, String> nicks = new HashMap<>();
    private boolean hideAll = false;

    public Map<UUID, String> getAllNicks() { return nicks; }

    public boolean isHideAll() { return hideAll; }

    public void setHideAll(boolean v) {
        hideAll = v;
        markDirty();
    }

    public String getNick(UUID id) { return nicks.getOrDefault(id, ""); }

    public void setNick(UUID id, String nick) {
        if (nick == null || nick.isBlank()) nicks.remove(id);
        else nicks.put(id, nick);
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("hideAll", hideAll);

        NbtList list = new NbtList();
        for (var e : nicks.entrySet()) {
            NbtCompound row = new NbtCompound();
            row.putUuid("uuid", e.getKey());
            row.putString("nick", e.getValue());
            list.add(row);
        }
        nbt.put("nicks", list);
        return nbt;
    }

    public static NickState fromNbt(NbtCompound nbt) {
        NickState st = new NickState();
        st.hideAll = nbt.getBoolean("hideAll");

        NbtList list = nbt.getList("nicks", 10);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound row = list.getCompound(i);
            UUID id = row.getUuid("uuid");
            String nick = row.getString("nick");
            if (nick != null && !nick.isBlank()) st.nicks.put(id, nick);
        }
        return st;
    }

    public static NickState get(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        PersistentStateManager psm = world.getPersistentStateManager();
        return psm.getOrCreate(NickState::fromNbt, NickState::new, "name_changer_state");
    }
}
