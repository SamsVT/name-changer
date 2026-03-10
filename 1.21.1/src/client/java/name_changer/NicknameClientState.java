package name_changer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NicknameClientState {
    private static final Map<UUID, String> NICKNAMES = new HashMap<>();
    private static boolean hideAllNames;
    private static boolean showHiddenNames;

    private NicknameClientState() {
    }

    public static void applySyncState(Map<UUID, String> nicknames, boolean hideAllNames) {
        NICKNAMES.clear();
        NICKNAMES.putAll(nicknames);
        NicknameClientState.hideAllNames = hideAllNames;
    }

    public static void setNickname(UUID playerId, String nickname) {
        if (nickname == null || nickname.isBlank()) {
            NICKNAMES.remove(playerId);
            return;
        }

        NICKNAMES.put(playerId, nickname);
    }

    public static void setHideAllNames(boolean hideAllNames) {
        NicknameClientState.hideAllNames = hideAllNames;
    }

    public static void setShowHiddenNames(boolean showHiddenNames) {
        NicknameClientState.showHiddenNames = showHiddenNames;
    }

    public static void clear() {
        NICKNAMES.clear();
        hideAllNames = false;
        showHiddenNames = false;
    }

    public static String getNicknameOrRealName(UUID playerId, String realName) {
        String nickname = NICKNAMES.get(playerId);
        if (nickname == null || nickname.isBlank()) {
            return realName;
        }

        return nickname;
    }

    public static boolean shouldHideOverheadName(Entity entity) {
        return entity instanceof PlayerEntity && hideAllNames && !showHiddenNames;
    }

    public static Text getOverheadDisplayName(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return entity.getDisplayName();
        }

        String resolvedName = getNicknameOrRealName(player.getUuid(), player.getGameProfile().getName());
        return Text.literal(resolvedName);
    }

    public static Text getTabDisplayName(UUID playerId, String realName) {
        String nickname = NICKNAMES.get(playerId);
        if (nickname == null || nickname.isBlank()) {
            return null;
        }

        return Text.literal(nickname);
    }

    public static Text getChatDisplayName(UUID playerId, String realName) {
        return Text.literal(getNicknameOrRealName(playerId, realName));
    }

    public static String getProfileName(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }

        for (PlayerEntity player : client.world.getPlayers()) {
            if (playerId.equals(player.getUuid())) {
                return player.getGameProfile().getName();
            }
        }

        return null;
    }

    public static UUID findPlayerIdByProfileName(String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            return null;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }

        for (PlayerEntity player : client.world.getPlayers()) {
            if (profileName.equalsIgnoreCase(player.getGameProfile().getName())) {
                return player.getUuid();
            }
        }

        return null;
    }

    public static UUID findPlayerIdByEntityId(int entityId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            return null;
        }

        Entity entity = client.world.getEntityById(entityId);
        if (entity instanceof PlayerEntity player) {
            return player.getUuid();
        }

        return null;
    }
}
