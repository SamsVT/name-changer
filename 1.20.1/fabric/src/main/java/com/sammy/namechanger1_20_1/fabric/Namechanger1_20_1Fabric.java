package com.sammy.namechanger1_20_1.fabric;

import com.sammy.namechanger1_20_1.Namechanger1_20_1;
import net.fabricmc.api.ModInitializer;

public final class Namechanger1_20_1Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        Namechanger1_20_1.init();
    }
}
