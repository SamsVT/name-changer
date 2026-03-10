package com.sammy.namechanger1_20_1.nickname;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

public final class NicknameSavedData extends SavedData {
    private static final String NICKNAMES_TAG = "Nicknames";
    private static final String HIDE_ALL_NAMES_TAG = "HideAllNames";
    private static final String PLAYER_UUID_TAG = "PlayerUuid";
    private static final String NICKNAME_TAG = "Nickname";

    private final Map<UUID, String> nicknames = new HashMap<>();
    private boolean hideAllNames;

    public static NicknameSavedData load(CompoundTag tag) {
        NicknameSavedData data = new NicknameSavedData();
        ListTag nicknameList = tag.getList(NICKNAMES_TAG, Tag.TAG_COMPOUND);

        for (int index = 0; index < nicknameList.size(); index++) {
            CompoundTag nicknameTag = nicknameList.getCompound(index);
            if (nicknameTag.hasUUID(PLAYER_UUID_TAG)) {
                String nickname = nicknameTag.getString(NICKNAME_TAG);
                if (!nickname.isEmpty()) {
                    data.nicknames.put(nicknameTag.getUUID(PLAYER_UUID_TAG), nickname);
                }
            }
        }

        data.hideAllNames = tag.getBoolean(HIDE_ALL_NAMES_TAG);
        return data;
    }

    public Map<UUID, String> getNicknames() {
        return nicknames;
    }

    public boolean isHideAllNames() {
        return hideAllNames;
    }

    public void setHideAllNames(boolean hideAllNames) {
        this.hideAllNames = hideAllNames;
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag nicknameList = new ListTag();

        for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
            CompoundTag nicknameTag = new CompoundTag();
            nicknameTag.putUUID(PLAYER_UUID_TAG, entry.getKey());
            nicknameTag.putString(NICKNAME_TAG, entry.getValue());
            nicknameList.add(nicknameTag);
        }

        tag.put(NICKNAMES_TAG, nicknameList);
        tag.putBoolean(HIDE_ALL_NAMES_TAG, hideAllNames);
        return tag;
    }
}
