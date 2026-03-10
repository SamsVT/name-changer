package com.sammy.namechanger1_20_1;

import com.sammy.namechanger1_20_1.command.NickCommand;
import com.sammy.namechanger1_20_1.nickname.NicknameService;

public final class Namechanger1_20_1 {
    public static final String MOD_ID = "namechanger1_20_1";

    public static void init() {
        NickCommand.register();
        NicknameService.init();
    }
}
