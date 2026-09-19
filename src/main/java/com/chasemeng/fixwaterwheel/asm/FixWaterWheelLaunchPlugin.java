package com.chasemeng.fixwaterwheel.asm;

import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.EnumSet;
import java.util.Set;

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
     * 声明本插件关心哪些类，并指定在哪个 Phase 进行处理。
     * 注意：在 1.20.1 中，此方法不需要 String reason 参数。
     */
    @Override
    public EnumSet<Phase> handlesClass(Type classType, boolean isEmpty) {
        String className = classType.getClassName();
        if (WATER_WHEEL_VISUAL.equals(className) || WATER_WHEEL_INSTANCE.equals(className)) {
            return EnumSet.of(Phase.BEFORE);
        }
        return EnumSet.noneOf(Phase.class);
    }

    /**
     * 对目标类进行字节码修改。
     * 注意：在 1.20.1 中，此方法的签名为 (Phase, ClassNode, Type)。
     */
    @Override
    public boolean processClass(Phase phase, ClassNode classNode, Type classType) {
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

    /**
     * 在 1.20.1 中，ILaunchPluginService 接口不再包含 initializeLaunch 方法。
     * 该方法已被移除，不再需要 @Override。
     */
    // @Override
    // public void initializeLaunch(ITransformerLoader transformerLoader, Path[] paths) {
    //     // 无需额外初始化
    // }

    /**
     * 在 1.20.1 中，addResources 是 default 方法，你可以选择不覆盖它。
     * 如果需要覆盖，其参数类型为 List<SecureJar>。
     */
    // @Override
    // public void addResources(List<SecureJar> resources) {
    //     // 无需额外资源
    // }

    /**
     * 在 1.20.1 中，ILaunchPluginService 接口不再包含 handshakePhases 方法。
     * 该方法已被移除，不再需要 @Override。
     */
    // @Override
    // public Set<Phase> handshakePhases() {
    //     return EnumSet.of(Phase.BEFORE);
    // }
}