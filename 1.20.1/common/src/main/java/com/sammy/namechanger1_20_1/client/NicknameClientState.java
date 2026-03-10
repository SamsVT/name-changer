package com.sammy.namechanger1_20_1.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.sammy.namechanger1_20_1.nickname.NicknameNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;

@Environment(EnvType.CLIENT)
public final class NicknameClientState {
    private static final Map<UUID, String> NICKNAMES = new HashMap<>();
    private static boolean hideAllNames;
    private static boolean showHiddenNames;

    private NicknameClientState() {
    }

    public static void applySyncState(NicknameNetworking.SyncState syncState) {
        NICKNAMES.clear();
        NICKNAMES.putAll(syncState.nicknames());
        hideAllNames = syncState.hideAllNames();
    }

    public static void clear() {
        NICKNAMES.clear();
        hideAllNames = false;
        showHiddenNames = false;
    }

    public static String getNicknameOrRealName(UUID playerId, String realName) {
        String nickname = NICKNAMES.get(playerId);
        if (nickname == null || nickname.isEmpty()) {
            return realName;
        }

        return nickname;
    }

    public static boolean shouldHideOverheadName(Entity entity) {
        return entity instanceof Player && hideAllNames && !shouldShowHiddenNames();
    }

    public static Component getOverheadDisplayName(Entity entity) {
        if (!(entity instanceof Player player)) {
            return entity.getDisplayName();
        }

        return createNicknameComponent(player.getUUID(), player.getTeam(), entity.getDisplayName());
    }

    public static Component getTabDisplayName(PlayerInfo playerInfo) {
        return createNicknameComponent(playerInfo.getProfile().getId(), playerInfo.getTeam(), null);
    }

    public static ChatType.Bound getChatDisplayName(ChatType.Bound bound, UUID playerId) {
        PlayerInfo playerInfo = getPlayerInfo(playerId);
        Component nicknameComponent = createNicknameComponent(playerId, playerInfo == null ? null : playerInfo.getTeam(), null);
        if (nicknameComponent == null) {
            return bound;
        }

        return new ChatType.Bound(bound.chatType(), nicknameComponent, bound.targetName());
    }

    public static void setShowHiddenNames(boolean showHiddenNames) {
        NicknameClientState.showHiddenNames = showHiddenNames;
    }

    public static String getProfileName(UUID playerId) {
        if (playerId == null) {
            return null;
        }

        PlayerInfo playerInfo = getPlayerInfo(playerId);
        if (playerInfo == null) {
            return null;
        }

        return playerInfo.getProfile().getName();
    }

    public static UUID findPlayerIdByProfileName(String profileName) {
        if (profileName == null || profileName.isEmpty()) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return null;
        }

        for (PlayerInfo playerInfo : minecraft.getConnection().getOnlinePlayers()) {
            if (profileName.equalsIgnoreCase(playerInfo.getProfile().getName())) {
                return playerInfo.getProfile().getId();
            }
        }

        return null;
    }

    public static UUID findPlayerIdByEntityId(int entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        Entity entity = minecraft.level.getEntity(entityId);
        if (entity instanceof Player player) {
            return player.getUUID();
        }

        return null;
    }

    private static boolean shouldShowHiddenNames() {
        return hideAllNames && showHiddenNames;
    }

    private static Component createNicknameComponent(UUID playerId, Team team, Component fallback) {
        String resolvedName = getNicknameOrRealName(playerId, null);
        if (resolvedName == null || resolvedName.isEmpty()) {
            return fallback;
        }

        return PlayerTeam.formatNameForTeam(team, Component.literal(resolvedName));
    }

    private static PlayerInfo getPlayerInfo(UUID playerId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return null;
        }

        return minecraft.getConnection().getPlayerInfo(playerId);
    }
}
