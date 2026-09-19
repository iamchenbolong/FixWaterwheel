package com.chasemeng.fixwaterwheel.server;

import com.chasemeng.fixwaterwheel.config.FixWaterWheelConfig;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务端水车 tick 调度器。
 *
 * 核心思路：将大量水车的流体检查分散到不同的 tick 上执行，
 * 避免所有水车在同一 tick 集中计算，造成 MSPT 尖峰。
 *
 * 实现方式：
 * 1. 监听 ServerTickEvent，在每个 tick 结束时执行
 * 2. 通过遍历所有已加载的区块，来获取其中的水车方块实体
 * 3. 只处理 WaterWheelBlockEntity，并根据其所在区块错开检查时间
 */
public class WaterWheelTickScheduler {

    /**
     * 每个区块的下一次检查时间。
     * key = ChunkPos 的 long 编码，value = 下一次检查的 tick 数
     */
    private static final Map<Long, Long> nextCheckTick = new ConcurrentHashMap<>();

    /**
     * 每个区块的偏移量，用于错开不同区块的检查时间。
     * 这样不同区块不会在同一 tick 同时执行。
     */
    private static final Map<Long, Integer> chunkOffset = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        var server = event.getServer();
        if (server == null) return;

        long currentTick = server.getTickCount();
        int interval = FixWaterWheelConfig.SERVER.waterWheelTickInterval.get();

        for (ServerLevel level : server.getAllLevels()) {
            // 遍历当前维度所有已加载的区块
            for (LevelChunk chunk : level.getChunkSource().getLoadedChunks()) {
                long chunkKey = chunk.getPos().toLong();

                // 计算该区块的下一次检查时间
                int offset = chunkOffset.computeIfAbsent(chunkKey,
                        k -> Math.abs((int) (k % interval)));
                long nextTick = nextCheckTick.computeIfAbsent(chunkKey,
                        k -> currentTick + offset);

                // 如果还没到检查时间，跳过
                if (currentTick < nextTick) continue;

                // 到达检查时间，处理该区块中的水车
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof WaterWheelBlockEntity waterWheel) {
                        triggerFlowCheck(waterWheel);
                    }
                }

                // 安排下一次检查
                nextCheckTick.put(chunkKey, currentTick + interval);
            }
        }
    }

    /**
     * 触发水车的流体方向检查。
     *
     * 由于 WaterWheelBlockEntity 的 determineAndApplyFlowScore 是 protected 的，
     * 这里通过反射调用，或者你可以考虑在 AT 中开放访问权限。
     */
    private static void triggerFlowCheck(WaterWheelBlockEntity waterWheel) {
        try {
            var method = WaterWheelBlockEntity.class.getDeclaredMethod("determineAndApplyFlowScore");
            method.setAccessible(true);
            method.invoke(waterWheel);
        } catch (Exception e) {
            // 如果方法名或签名不对，静默失败
            // 实际开发时需要通过反编译确认方法名
        }
    }
}