package com.chasemeng.fixwaterwheel;

import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfig;
import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfigScreen;
import com.chasemeng.fixwaterwheel.server.WaterWheelTickScheduler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(FixWaterWheel.MOD_ID)
public class FixWaterWheel {

    public static final String MOD_ID = "fixwaterwheel";

    public FixWaterWheel() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                FixWaterWheelConfig.CLIENT_SPEC,
                "fixwaterwheel-client.toml"
        );
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.SERVER,
                FixWaterWheelConfig.SERVER_SPEC,
                "fixwaterwheel-server.toml"
        );

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // 监听逻辑服务端的启动和停止，安全地注册/注销调度器
        MinecraftForge.EVENT_BUS.addListener(WaterWheelTickScheduler::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(WaterWheelTickScheduler::onServerStopped);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        FixWaterWheelConfigScreen.register();
    }
}