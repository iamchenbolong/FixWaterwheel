package com.chasemeng.fixwaterwheel.server;

import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfig;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WaterWheelTickScheduler {

    private static final Map<Long, Long> nextCheckTick = new ConcurrentHashMap<>();
    private static final Map<Long, Integer> chunkOffset = new ConcurrentHashMap<>();
    private static final Set<Long> loadedChunks = ConcurrentHashMap.newKeySet();

    /**
     * 在逻辑服务端启动时注册事件监听器。
     */
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftForge.EVENT_BUS.register(WaterWheelTickScheduler.class);
    }

    /**
     * 在逻辑服务端停止时注销事件监听器并清空缓存。
     */
    public static void onServerStopped(ServerStoppedEvent event) {
        MinecraftForge.EVENT_BUS.unregister(WaterWheelTickScheduler.class);
        loadedChunks.clear();
        nextCheckTick.clear();
        chunkOffset.clear();
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            loadedChunks.add(event.getChunk().getPos().toLong());
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel) {
            long chunkKey = event.getChunk().getPos().toLong();
            loadedChunks.remove(chunkKey);
            nextCheckTick.remove(chunkKey);
            chunkOffset.remove(chunkKey);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = event.getServer();
        if (server == null) return;

        long currentTick = server.getTickCount();
        int interval = FixWaterWheelConfig.SERVER.waterWheelTickInterval.get();

        for (ServerLevel level : server.getAllLevels()) {
            for (long chunkKey : loadedChunks) {
                int offset = chunkOffset.computeIfAbsent(chunkKey,
                        k -> Math.abs((int) (k % interval)));
                long nextTick = nextCheckTick.computeIfAbsent(chunkKey,
                        k -> currentTick + offset);

                if (currentTick < nextTick) continue;

                ChunkPos chunkPos = new ChunkPos(chunkKey);
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);

                if (chunk != null) {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof WaterWheelBlockEntity waterWheel) {
                            triggerFlowCheck(waterWheel);
                        }
                    }
                }

                nextCheckTick.put(chunkKey, currentTick + interval);
            }
        }
    }

    private static void triggerFlowCheck(WaterWheelBlockEntity waterWheel) {
        try {
            var method = WaterWheelBlockEntity.class.getDeclaredMethod("determineAndApplyFlowScore");
            method.setAccessible(true);
            method.invoke(waterWheel);
        } catch (Exception e) {
            // 静默失败
        }
    }
}