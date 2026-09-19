package com.chasemeng.fixwaterwheel.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * 对 WaterWheelInstance 进行字节码修改。
 *
 * 目标：完全剔除扇叶渲染。
 * 通过拦截扇叶 Instance 的创建或渲染调用，使其不产生任何绘制。
 */
public class WaterWheelInstanceTransformer implements Opcodes {

    public static void transform(ClassNode classNode) {
        for (MethodNode method : classNode.methods) {
            // 定位扇叶模型的实例创建方法
            if (method.name.equals("createBlades") || method.name.contains("Blade")) {
                injectBladeRemoval(method);
            }

            // 定位渲染方法
            if (method.name.equals("render") || method.name.contains("Render")) {
                injectRenderSkip(method);
            }
        }
    }

    /**
     * 在扇叶创建方法开头插入：
     * if (FixWaterWheelConfig.CLIENT.removeBlades.get()) {
     *     return null;
     * }
     */
    private static void injectBladeRemoval(MethodNode method) {
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
                "removeBlades",
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
        insnList.add(new InsnNode(ACONST_NULL));
        insnList.add(new InsnNode(ARETURN));
        insnList.add(skipLabel);

        method.instructions.insert(insnList);
    }

    /**
     * 在渲染方法开头插入：
     * if (FixWaterWheelConfig.CLIENT.removeBlades.get()) {
     *     return;
     * }
     */
    private static void injectRenderSkip(MethodNode method) {
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
                "removeBlades",
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