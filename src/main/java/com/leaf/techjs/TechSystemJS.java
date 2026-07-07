package com.leaf.techjs;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(TechSystemJS.MOD_ID)
public class TechSystemJS {

    public static final String MOD_ID = "techjs";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @SuppressWarnings("removal")
    public TechSystemJS() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AllArgumentTypeInfos.register(modEventBus);
        AllPackets.registerPackets();

        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AllConfig.SPEC);
    }
}
