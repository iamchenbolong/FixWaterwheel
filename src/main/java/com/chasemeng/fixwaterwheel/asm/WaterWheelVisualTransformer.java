package com.chasemeng.fixwaterwheel.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * 对 WaterWheelVisual 进行字节码修改。
 *
 * 目标：
 * 1. 固定光照：在光照计算处强制使用全亮值
 * 2. 剔除旋转：在旋转角度更新处强制归零
 */
public class WaterWheelVisualTransformer implements Opcodes {

    public static void transform(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            // 定位光照更新方法
            if (method.name.equals("updateLight") || method.name.contains("Light")) {
                injectFixedLighting(method);
            }

            // 定位旋转更新方法
            if (method.name.equals("updateRotation") || method.name.contains("Rotation")) {
                injectZeroRotation(method);
            }
        }
    }

    /**
     * 在光照更新方法开头插入：
     * if (FixWaterWheelConfig.CLIENT.fixedLighting.get()) {
     *     // 强制使用全亮值
     *     light = 0xF000F0;
     * }
     */
    private static void injectFixedLighting(MethodNode method) {
        InsnList insnList = new InsnList();

        // 调用配置检查
        insnList.add(new FieldInsnNode(
                GETSTATIC,
                "com/chasemeng/fixwaterwheel/config/FixWaterWheelConfig",
                "CLIENT",
                "Lcom/chasemeng/fixwaterwheel/config/FixWaterWheelConfig$Client;"
        ));
        insnList.add(new FieldInsnNode(
                GETFIELD,
                "com/chasemeng/fixwaterwheel/config/FixWaterWheelConfig$Client",
                "fixedLighting",
                "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"
        ));
        insnList.add(new MethodInsnNode(
                INVOKEINTERFACE,
                "net/minecraftforge/common/ForgeConfigSpec$BooleanValue",
                "get",
                "()Ljava/lang/Object;",
                true
        ));
        insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
        insnList.add(new MethodInsnNode(
                INVOKEVIRTUAL,
                "java/lang/Boolean",
                "booleanValue",
                "()Z",
                false
        ));

        LabelNode skipLabel = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, skipLabel));

        // 强制设置光照为全亮
        insnList.add(new IntInsnNode(SIPUSH, 0xF000));
        insnList.add(new IntInsnNode(SIPUSH, 0xF000));
        insnList.add(new MethodInsnNode(
                INVOKESTATIC,
                "net/minecraft/client/renderer/LightTexture",
                "pack",
                "(II)I",
                false
        ));
        insnList.add(new VarInsnNode(ISTORE, 1)); // 假设 light 是局部变量1

        insnList.add(skipLabel);

        method.instructions.insert(insnList);
    }

    /**
     * 在旋转更新方法开头插入：
     * if (FixWaterWheelConfig.CLIENT.disableRotation.get()) {
     *     return; // 直接返回，不更新旋转
     * }
     */
    private static void injectZeroRotation(MethodNode method) {
        InsnList insnList = new InsnList();

        insnList.add(new FieldInsnNode(
                GETSTATIC,
                "com/chasemeng/fixwaterwheel/config/FixWaterWheelConfig",
                "CLIENT",
                "Lcom/chasemeng/fixwaterwheel/config/FixWaterWheelConfig$Client;"
        ));
        insnList.add(new FieldInsnNode(
                GETFIELD,
                "com/chasemeng/fixwaterwheel/config/FixWaterWheelConfig$Client",
                "disableRotation",
                "Lnet/minecraftforge/common/ForgeConfigSpec$BooleanValue;"
        ));
        insnList.add(new MethodInsnNode(
                INVOKEINTERFACE,
                "net/minecraftforge/common/ForgeConfigSpec$BooleanValue",
                "get",
                "()Ljava/lang/Object;",
                true
        ));
        insnList.add(new TypeInsnNode(CHECKCAST, "java/lang/Boolean"));
        insnList.add(new MethodInsnNode(
                INVOKEVIRTUAL,
                "java/lang/Boolean",
                "booleanValue",
                "()Z",
                false
        ));

        LabelNode skipLabel = new LabelNode();
        insnList.add(new JumpInsnNode(IFEQ, skipLabel));
        insnList.add(new InsnNode(RETURN));
        insnList.add(skipLabel);

        method.instructions.insert(insnList);
    }
}