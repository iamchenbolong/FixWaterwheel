package com.chasemeng.fixwaterwheel.server;

import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfig;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WaterWheelTickScheduler {

    /**
     * 每个已加载区块的下一次检查时间。
     */
    private static final Map<Long, Long> nextCheckTick = new ConcurrentHashMap<>();

    /**
     * 每个区块的偏移量，用于错开不同区块的检查时间。
     */
    private static final Map<Long, Integer> chunkOffset = new ConcurrentHashMap<>();

    /**
     * 记录当前所有已加载的区块。
     * 通过 ChunkEvent.Load 和 ChunkEvent.Unload 事件来维护。
     */
    private static final Set<Long> loadedChunks = ConcurrentHashMap.newKeySet();

    /**
     * 监听区块加载事件。
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel) {
            loadedChunks.add(event.getChunk().getPos().toLong());
        }
    }

    /**
     * 监听区块卸载事件。
     */
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
                // 计算该区块的下一次检查时间
                int offset = chunkOffset.computeIfAbsent(chunkKey,
                        k -> Math.abs((int) (k % interval)));
                long nextTick = nextCheckTick.computeIfAbsent(chunkKey,
                        k -> currentTick + offset);

                // 如果还没到检查时间，跳过
                if (currentTick < nextTick) continue;

                // 通过区块坐标获取区块
                ChunkPos chunkPos = new ChunkPos(chunkKey);
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);

                if (chunk != null) {
                    for (BlockEntity be : chunk.getBlockEntities().values()) {
                        if (be instanceof WaterWheelBlockEntity waterWheel) {
                            triggerFlowCheck(waterWheel);
                        }
                    }
                }

                // 安排下一次检查
                nextCheckTick.put(chunkKey, currentTick + interval);
            }
        }
    }

    /**
     * 触发水车的流体方向检查。
     */
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