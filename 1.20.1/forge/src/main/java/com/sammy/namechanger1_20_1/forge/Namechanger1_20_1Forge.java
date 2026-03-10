package com.sammy.namechanger1_20_1.forge;

import com.sammy.namechanger1_20_1.Namechanger1_20_1;
import com.sammy.namechanger1_20_1.client.Namechanger1_20_1Client;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Namechanger1_20_1.MOD_ID)
public final class Namechanger1_20_1Forge {
    public Namechanger1_20_1Forge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Namechanger1_20_1.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Namechanger1_20_1.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Namechanger1_20_1Client::init);
    }
}
