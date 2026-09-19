package com.chasemeng.fixwaterwheel.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class FixWaterWheelConfig {

    // ==================== 客户端配置 ====================
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    // ==================== 服务端配置 ====================
    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        Pair<Client, ForgeConfigSpec> clientPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        CLIENT_SPEC = clientPair.getRight();

        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = serverPair.getLeft();
        SERVER_SPEC = serverPair.getRight();
    }

    /**
     * 客户端配置：只影响本地渲染，不改变服务端逻辑。
     */
    public static class Client {
        public final ForgeConfigSpec.BooleanValue fixedLighting;
        public final ForgeConfigSpec.BooleanValue disableRotation;
        public final ForgeConfigSpec.BooleanValue removeBlades;

        Client(ForgeConfigSpec.Builder builder) {
            builder.comment(
                    "==================== Fix Water Wheel 客户端配置 ====================",
                    "这些选项只影响本地客户端的渲染效果，",
                    "不会改变水车在服务端的实际行为（应力输出、转速等）。",
                    "服务端仍然会正常计算水车的动力。",
                    "==================================================================="
            ).push("client");

            fixedLighting = builder
                    .comment(
                            "【固定光照】",
                            "开启后，水车始终以最大亮度（光照等级15）渲染，",
                            "不再根据周围环境光照改变亮度。",
                            "对大量水车场景可减少光照计算开销。",
                            "默认：关闭"
                    )
                    .define("fixedLighting", false);

            disableRotation = builder
                    .comment(
                            "【剔除扇叶旋转】",
                            "开启后，水车的扇叶将停止旋转动画，",
                            "但水车中间的中心轴仍然会正常旋转，",
                            "让玩家能看出水车正在工作。",
                            "服务端的应力输出和转速计算完全不受影响。",
                            "默认：关闭"
                    )
                    .define("disableRotation", false);

            removeBlades = builder
                    .comment(
                            "【完全剔除扇叶】",
                            "开启后，水车的扇叶将完全不再渲染，",
                            "只保留中心轴。这是最激进的优化选项，",
                            "适合极端大量水车场景。",
                            "服务端的应力输出和转速计算完全不受影响。",
                            "默认：关闭"
                    )
                    .define("removeBlades", false);

            builder.pop();
        }
    }

    /**
     * 服务端配置：影响水车在服务端的 tick 行为。
     */
    public static class Server {
        public final ForgeConfigSpec.IntValue waterWheelTickInterval;

        Server(ForgeConfigSpec.Builder builder) {
            builder.comment(
                    "==================== Fix Water Wheel 服务端配置 ====================",
                    "这些选项影响水车在服务端的 tick 行为，",
                    "会改变水车的实际检查频率。",
                    "==================================================================="
            ).push("server");

            waterWheelTickInterval = builder
                    .comment(
                            "【水车流体检查间隔】",
                            "水车原本每 60 刻检查一次周围流体方向，",
                            "并据此计算转速和应力输出。",
                            "增大此值可以显著降低大量水车带来的服务端 tick 压力，",
                            "但水车对流体变化的响应会变慢。",
                            "范围：20 ~ 600 刻，默认：60 刻（与原版一致）"
                    )
                    .defineInRange("waterWheelTickInterval", 60, 20, 600);

            builder.pop();
        }
    }
}