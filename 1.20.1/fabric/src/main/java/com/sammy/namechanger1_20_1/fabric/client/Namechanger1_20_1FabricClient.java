package com.sammy.namechanger1_20_1.fabric.client;

import com.sammy.namechanger1_20_1.client.Namechanger1_20_1Client;
import net.fabricmc.api.ClientModInitializer;

public final class Namechanger1_20_1FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Namechanger1_20_1Client.init();
    }
}
