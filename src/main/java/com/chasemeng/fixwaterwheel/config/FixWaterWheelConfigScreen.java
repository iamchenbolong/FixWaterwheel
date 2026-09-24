package com.chasemeng.fixwaterwheel.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
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

        // 创建输入框
        EditBox intervalBox = new EditBox(this.font, centerX - buttonWidth / 2, y, buttonWidth, 20, Component.literal("间隔刻数"));
        intervalBox.setValue(String.valueOf(FixWaterWheelConfig.SERVER.waterWheelTickInterval.get()));
        intervalBox.setHint(Component.literal("输入 20-600 之间的刻数"));
        // 限制只能输入数字
        intervalBox.setFilter(text -> text.matches("\\d*"));
        addRenderableWidget(intervalBox);

        // 完成按钮，负责读取输入框并应用配置
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
            try {
                int val = Integer.parseInt(intervalBox.getValue());
                if (val >= 20 && val <= 600) {
                    if (FixWaterWheelConfig.SERVER_SPEC.isLoaded()) {
                        // 在单人游戏或集成服务器中，配置已加载，可以安全设置
                        FixWaterWheelConfig.SERVER.waterWheelTickInterval.set(val);
                    } else {
                        // 在多人游戏客户端中，服务端配置未加载，无法直接修改
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.displayClientMessage(
                                    Component.literal("§c无法修改服务器配置：当前不在单人游戏中或服务器未同步配置！"), false);
                        }
                    }
                } else {
                    if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(
                                Component.literal("§c间隔刻数必须在 20 到 600 之间！"), false);
                    }
                }
            } catch (NumberFormatException ignored) {
                // 输入框为空或非数字时忽略
            }
            this.minecraft.setScreen(this.parent);
        }).pos(centerX - 60, this.height - 40).width(120).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        int centerX = this.width / 2;
        int yStart = this.height / 4;

        // 分区标题
        guiGraphics.drawString(this.font,
                "§e===== 客户端配置（仅本地渲染，不影响服务端） =====",
                centerX - 160, yStart + 10, 0xFFFF55);
        guiGraphics.drawString(this.font,
                "§a===== 服务端配置（影响水车实际行为） =====",
                centerX - 160, yStart + 104, 0x55FF55);

        // 输入框上方的文字说明
        guiGraphics.drawString(this.font,
                "水车流体检查间隔（刻）:",
                centerX - 100, yStart + 124 - 12, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}