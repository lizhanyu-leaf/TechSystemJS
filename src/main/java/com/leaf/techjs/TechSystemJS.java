package com.leaf.techjs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @SuppressWarnings("removal")
    public TechSystemJS() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AllArgumentTypeInfos.register(modEventBus);
        AllPackets.registerPackets();
        AllItems.register(modEventBus);

        FMLJavaModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AllConfig.SPEC);
    }
}
