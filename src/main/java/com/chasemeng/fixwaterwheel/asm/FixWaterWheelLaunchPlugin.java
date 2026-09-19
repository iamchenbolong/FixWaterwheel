package com.chasemeng.fixwaterwheel.asm;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;
import java.util.Set;

/**
 * 通过 ILaunchPluginService 实现字节码修改，替代 Mixin。
 *
 * 该插件在游戏类被加载进内存之前获得其字节码，
 * 可以修改 Create 水车渲染相关的类。
 */
public class FixWaterWheelLaunchPlugin implements ILaunchPluginService {

    private static final String WATER_WHEEL_VISUAL =
            "com.simibubi.create.content.kinetics.waterwheel.WaterWheelVisual";
    private static final String WATER_WHEEL_INSTANCE =
            "com.simibubi.create.content.kinetics.waterwheel.WaterWheelInstance";

    @Override
    public String name() {
        return "FixWaterWheel";
    }

    /**
     * 声明本插件关心哪些类。
     * 返回 true 表示需要对该类调用 processClassWithFlags。
     */
    @Override
    public boolean handlesClass(Type classType, boolean isEmpty) {
        String className = classType.getClassName();
        return WATER_WHEEL_VISUAL.equals(className)
                || WATER_WHEEL_INSTANCE.equals(className);
    }

    /**
     * 对目标类进行字节码修改。
     * 这里根据配置决定是否注入修改。
     */
    @Override
    public boolean processClass(ClassNode classNode, Type classType, EnumSet<Phase> phases) {
        String className = classType.getClassName();

        if (WATER_WHEEL_VISUAL.equals(className)) {
            WaterWheelVisualTransformer.transform(classNode);
            return true;
        }

        if (WATER_WHEEL_INSTANCE.equals(className)) {
            WaterWheelInstanceTransformer.transform(classNode);
            return true;
        }

        return false;
    }

    @Override
    public void initializeLaunch(ITransformerLoader transformerLoader, java.nio.file.Path[] paths) {
        // 无需额外初始化
    }

    @Override
    public void addResources(java.util.List<java.nio.file.Path> resources) {
        // 无需额外资源
    }

    @Override
    public Set<Phase> handshakePhases() {
        return EnumSet.of(Phase.BEFORE);
    }
}