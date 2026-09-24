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

    private static final int WIDGET_HEIGHT = 20;
    private static final int WIDGET_GAP = 5;
    private static final int BUTTON_WIDTH = 200;

    private static final Component TITLE = Component.translatable("fixwaterwheel.config.title");
    private static final Component SECTION_CLIENT = Component.translatable("fixwaterwheel.config.section.client");
    private static final Component SECTION_SERVER = Component.translatable("fixwaterwheel.config.section.server");
    private static final Component OPT_FIXED_LIGHTING = Component.translatable("fixwaterwheel.config.option.fixed_lighting");
    private static final Component OPT_DISABLE_ROTATION = Component.translatable("fixwaterwheel.config.option.disable_rotation");
    private static final Component OPT_REMOVE_BLADES = Component.translatable("fixwaterwheel.config.option.remove_blades");
    private static final Component OPT_TICK_INTERVAL = Component.translatable("fixwaterwheel.config.option.tick_interval");
    private static final Component HINT_TICK_INTERVAL = Component.translatable("fixwaterwheel.config.hint.tick_interval");
    private static final Component HINT_SERVER_UNAVAILABLE = Component.translatable("fixwaterwheel.config.hint.server_unavailable");
    private static final Component HINT_MENU = Component.translatable("fixwaterwheel.config.hint.menu");
    private static final Component ERROR_RANGE = Component.translatable("fixwaterwheel.config.error.range");

    private int clientSectionY;
    private int serverSectionY;
    private int inputLabelY;
    private int inputY;
    private int warningY;

    public FixWaterWheelConfigScreen(Screen parent) {
        super(TITLE);
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

    /**
     * 判断服务端配置是否可用。返回 null 表示可用；否则返回需要显示的提示文本。
     * 注意：这只影响“服务端配置”区域，不影响客户端配置的保存。
     */
    private static Component getServerUnavailableMessage() {
        if (FixWaterWheelConfig.SERVER_SPEC.isLoaded()) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return HINT_MENU;
        }
        if (mc.hasSingleplayerServer()) {
            return null;
        }
        return HINT_SERVER_UNAVAILABLE;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int left = centerX - BUTTON_WIDTH / 2;
        int y = 30;

        // ========== 客户端配置区域 ==========
        this.clientSectionY = y;
        y += 14;

        addRenderableWidget(CycleButton
                .onOffBuilder(FixWaterWheelConfig.CLIENT.fixedLighting.get())
                .create(left, y, BUTTON_WIDTH, WIDGET_HEIGHT, OPT_FIXED_LIGHTING,
                        (btn, value) -> FixWaterWheelConfig.CLIENT.fixedLighting.set(value)));
        y += WIDGET_HEIGHT + WIDGET_GAP;

        addRenderableWidget(CycleButton
                .onOffBuilder(FixWaterWheelConfig.CLIENT.disableRotation.get())
                .create(left, y, BUTTON_WIDTH, WIDGET_HEIGHT, OPT_DISABLE_ROTATION,
                        (btn, value) -> FixWaterWheelConfig.CLIENT.disableRotation.set(value)));
        y += WIDGET_HEIGHT + WIDGET_GAP;

        addRenderableWidget(CycleButton
                .onOffBuilder(FixWaterWheelConfig.CLIENT.removeBlades.get())
                .create(left, y, BUTTON_WIDTH, WIDGET_HEIGHT, OPT_REMOVE_BLADES,
                        (btn, value) -> FixWaterWheelConfig.CLIENT.removeBlades.set(value)));
        y += WIDGET_HEIGHT + 20;  // 客户端区域结束，留出间隔

        // ========== 服务端配置区域 ==========
        this.serverSectionY = y;
        y += 14;

        this.inputLabelY = y;
        y += 12;

        this.inputY = y;
        Component unavailableMsg = getServerUnavailableMessage();
        boolean unavailable = unavailableMsg != null;

        EditBox intervalBox = new EditBox(this.font, left, y, BUTTON_WIDTH, WIDGET_HEIGHT, OPT_TICK_INTERVAL);
        if (unavailable) {
            intervalBox.setValue("60");
            intervalBox.setHint(HINT_TICK_INTERVAL);
            intervalBox.setEditable(false);
            intervalBox.setFilter(text -> false);
        } else {
            int current = 60;
            try {
                if (FixWaterWheelConfig.SERVER_SPEC.isLoaded()) {
                    current = FixWaterWheelConfig.SERVER.waterWheelTickInterval.get();
                }
            } catch (Exception ignored) {
            }
            intervalBox.setValue(String.valueOf(current));
            intervalBox.setHint(HINT_TICK_INTERVAL);
            intervalBox.setFilter(text -> text.matches("\\d*"));
        }
        addRenderableWidget(intervalBox);

        this.warningY = this.inputY + WIDGET_HEIGHT + 6;

        // ========== 完成按钮固定屏幕底部 ==========
        int doneY = this.height - 30;
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> {
            // 客户端配置通过 CycleButton 的 set() 已即时生效，无需额外处理。
            // 只有当服务端配置可用时，才读取输入框的值并应用。
            if (FixWaterWheelConfig.SERVER_SPEC.isLoaded()) {
                try {
                    int val = Integer.parseInt(intervalBox.getValue());
                    if (val >= 20 && val <= 600) {
                        FixWaterWheelConfig.SERVER.waterWheelTickInterval.set(val);
                    } else if (this.minecraft != null && this.minecraft.player != null) {
                        this.minecraft.player.displayClientMessage(ERROR_RANGE, false);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            // 服务端不可用时：静默关闭，不弹任何提示。
            // 用户可能只是改了客户端配置，改客户端配置与服务器无关。
            this.minecraft.setScreen(this.parent);
        }).pos(centerX - 60, doneY).size(120, WIDGET_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gg);
        super.render(gg, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;

        // 标题
        gg.drawCenteredString(this.font, this.title, centerX, 12, 0xFFFFFF);

        // 分区标题与按钮左边缘对齐
        int left = centerX - BUTTON_WIDTH / 2;
        gg.drawString(this.font, SECTION_CLIENT, left, clientSectionY, 0xFFFF55);
        gg.drawString(this.font, SECTION_SERVER, left, serverSectionY, 0x55FF55);

        // 输入框标签
        gg.drawString(this.font, OPT_TICK_INTERVAL, left, inputLabelY, 0xAAAAAA);

        // 服务端不可用时的红色警告
        Component msg = getServerUnavailableMessage();
        if (msg != null) {
            gg.drawString(this.font, msg, left, warningY, 0xFF5555);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}