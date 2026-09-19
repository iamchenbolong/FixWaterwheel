package com.chasemeng.fixwaterwheel;

import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfig;
import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfigScreen;
import com.chasemeng.fixwaterwheel.server.WaterWheelTickScheduler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
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
        // 注册配置
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
        // 服务端调度器只在逻辑服务端生效
        if (!FMLEnvironment.dist.isClient() || FMLEnvironment.dist.isDedicatedServer()) {
            MinecraftForge.EVENT_BUS.register(WaterWheelTickScheduler.class);
        }
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        // 在客户端注册配置界面
        FixWaterWheelConfigScreen.register();
    }
}