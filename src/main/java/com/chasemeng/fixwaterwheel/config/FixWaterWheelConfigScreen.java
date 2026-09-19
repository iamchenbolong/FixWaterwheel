package com.chasemeng.fixwaterwheel.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

@OnlyIn(Dist.CLIENT)
public class FixWaterWheelConfigScreen extends Screen {

    private final Screen parent;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP = 4;

    public FixWaterWheelConfigScreen(Screen parent) {
        super(Component.literal("Fix Water Wheel 配置"));
        this.parent = parent;
    }

    /**
     * 用 Forge 1.20.1 的 registerConfigScreen API 注册配置界面。
     * 这是 Forge 在 1.20.x 推荐的配置界面注册方式。
     */
    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> new FixWaterWheelConfigScreen(parent)
                )
        );
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 4;
        int buttonWidth = 200;

        // ========== 客户端配置区域 ==========
        y += 20;

        addRenderableWidget(CycleButton
                .onOffBuilder(FixWaterWheelConfig.CLIENT.fixedLighting.get())
                .create(centerX - buttonWidth / 2, y, buttonWidth, 20,
                        Component.literal("固定光照 (客户端)"),
                        (btn, value) -> FixWaterWheelConfig.CLIENT.fixedLighting.set(value)));
        y += ROW_HEIGHT + ROW_GAP;

        addRenderableWidget(CycleButton
                .onOffBuilder(FixWaterWheelConfig.CLIENT.disableRotation.get())
                .create(centerX - buttonWidth / 2, y, buttonWidth, 20,
                        Component.literal("剔除扇叶旋转 (客户端)"),
                        (btn, value) -> FixWaterWheelConfig.CLIENT.disableRotation.set(value)));
        y += ROW_HEIGHT + ROW_GAP;

        addRenderableWidget(CycleButton
                .onOffBuilder(FixWaterWheelConfig.CLIENT.removeBlades.get())
                .create(centerX - buttonWidth / 2, y, buttonWidth, 20,
                        Component.literal("完全剔除扇叶 (客户端)"),
                        (btn, value) -> FixWaterWheelConfig.CLIENT.removeBlades.set(value)));
        y += ROW_HEIGHT + ROW_GAP * 3;

        // ========== 服务端配置区域 ==========
        y += 20;

        addRenderableWidget(new Button.Builder(
                Component.literal("流体检查间隔: " + FixWaterWheelConfig.SERVER.waterWheelTickInterval.get() + " 刻"),
                btn -> {
                    int current = FixWaterWheelConfig.SERVER.waterWheelTickInterval.get();
                    int next;
                    if (current >= 300) next = 20;
                    else if (current >= 120) next = 600;
                    else if (current >= 60) next = 300;
                    else next = 120;
                    FixWaterWheelConfig.SERVER.waterWheelTickInterval.set(next);
                    btn.setMessage(Component.literal("流体检查间隔: " + next + " 刻"));
                })
                .pos(centerX - buttonWidth / 2, y)
                .width(buttonWidth)
                .build());
        y += ROW_HEIGHT + ROW_GAP;

        // 完成按钮
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
            this.minecraft.setScreen(this.parent);
        }).pos(centerX - 60, this.height - 40).width(120).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // 分区标题
        int y = this.height / 4;
        guiGraphics.drawString(this.font,
                "§e===== 客户端配置（仅本地渲染，不影响服务端） =====",
                this.width / 2 - 160, y + 10, 0xFFFF55);
        guiGraphics.drawString(this.font,
                "§a===== 服务端配置（影响水车实际行为） =====",
                this.width / 2 - 160, y + 135, 0x55FF55);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}