package dev.invisibleesp.gui;

import dev.invisibleesp.config.EspConfig;
import dev.invisibleesp.input.KeyBindings;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * Settings menu (opened with RIGHT SHIFT by default).
 */
public final class EspSettingsScreen extends Screen {
    private static final int COLUMN_WIDTH = 150;
    private static final int COLUMN_GAP = 10;
    private static final int ROW_HEIGHT = 24;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen parent;

    private ButtonWidget espButton;
    private ButtonWidget nameButton;
    private ButtonWidget wallsButton;

    private int swatchX;
    private int swatchY;

    public EspSettingsScreen(Screen parent) {
        super(Text.translatable("gui.invisibleesp.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        EspConfig config = EspConfig.get();

        int left = this.width / 2 - COLUMN_WIDTH - COLUMN_GAP / 2;
        int right = this.width / 2 + COLUMN_GAP / 2;
        int top = Math.max(40, this.height / 2 - 70);

        // Left column: switches, distance, brightness.
        espButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            config.enabled = !config.enabled;
            refreshButtons();
        }).dimensions(left, top, COLUMN_WIDTH, WIDGET_HEIGHT).build());

        nameButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            config.showName = !config.showName;
            refreshButtons();
        }).dimensions(left, top + ROW_HEIGHT, COLUMN_WIDTH, WIDGET_HEIGHT).build());

        wallsButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            config.throughWalls = !config.throughWalls;
            refreshButtons();
        }).dimensions(left, top + 2 * ROW_HEIGHT, COLUMN_WIDTH, WIDGET_HEIGHT).build());

        this.addDrawableChild(new ConfigSlider(
                left, top + 3 * ROW_HEIGHT, COLUMN_WIDTH, WIDGET_HEIGHT,
                EspConfig.MIN_DISTANCE, EspConfig.MAX_DISTANCE, config.renderDistance, 1,
                value -> Text.translatable("gui.invisibleesp.render_distance", (int) value),
                value -> config.renderDistance = (int) value));

        this.addDrawableChild(new ConfigSlider(
                left, top + 4 * ROW_HEIGHT, COLUMN_WIDTH, WIDGET_HEIGHT,
                EspConfig.MIN_BRIGHTNESS, EspConfig.MAX_BRIGHTNESS, config.brightness, 1,
                value -> Text.translatable("gui.invisibleesp.brightness", (int) value),
                value -> config.brightness = (int) value));

        // Right column: colour channels and a live preview swatch.
        this.addDrawableChild(new ConfigSlider(
                right, top, COLUMN_WIDTH, WIDGET_HEIGHT, 0, 255, config.red, 1,
                value -> Text.translatable("gui.invisibleesp.color_red", (int) value),
                value -> config.red = (int) value));

        this.addDrawableChild(new ConfigSlider(
                right, top + ROW_HEIGHT, COLUMN_WIDTH, WIDGET_HEIGHT, 0, 255, config.green, 1,
                value -> Text.translatable("gui.invisibleesp.color_green", (int) value),
                value -> config.green = (int) value));

        this.addDrawableChild(new ConfigSlider(
                right, top + 2 * ROW_HEIGHT, COLUMN_WIDTH, WIDGET_HEIGHT, 0, 255, config.blue, 1,
                value -> Text.translatable("gui.invisibleesp.color_blue", (int) value),
                value -> config.blue = (int) value));

        swatchX = right;
        swatchY = top + 3 * ROW_HEIGHT;

        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), button -> this.close())
                .dimensions(this.width / 2 - 100, top + 5 * ROW_HEIGHT + 8, 200, WIDGET_HEIGHT)
                .build());

        refreshButtons();
    }

    private void refreshButtons() {
        EspConfig config = EspConfig.get();
        espButton.setMessage(Text.translatable("gui.invisibleesp.esp", onOff(config.enabled)));
        nameButton.setMessage(Text.translatable("gui.invisibleesp.player_name", onOff(config.showName)));
        wallsButton.setMessage(Text.translatable("gui.invisibleesp.through_walls", onOff(config.throughWalls)));
    }

    private static Text onOff(boolean value) {
        return Text.translatable(value ? "options.on" : "options.off")
                .formatted(value ? Formatting.GREEN : Formatting.RED);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Screen#render draws the background and every widget.
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);

        // Preview of the final outline colour (brightness included).
        context.fill(swatchX - 1, swatchY - 1, swatchX + COLUMN_WIDTH + 1, swatchY + WIDGET_HEIGHT + 1, 0xFFFFFFFF);
        context.fill(swatchX, swatchY, swatchX + COLUMN_WIDTH, swatchY + WIDGET_HEIGHT,
                0xFF000000 | EspConfig.get().outlineRgb());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (KeyBindings.openMenuKey() != null && KeyBindings.openMenuKey().matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(parent);
        }
    }

    @Override
    public void removed() {
        EspConfig.save();
    }

    /**
     * Slider that maps its 0..1 position to [min, max], rounds to a step and reports the value.
     */
    private static final class ConfigSlider extends SliderWidget {
        private final double min;
        private final double max;
        private final double step;
        private final DoubleFunction<Text> label;
        private final DoubleConsumer onChange;

        ConfigSlider(int x, int y, int width, int height, double min, double max, double current, double step,
                     DoubleFunction<Text> label, DoubleConsumer onChange) {
            super(x, y, width, height, Text.empty(), (current - min) / (max - min));
            this.min = min;
            this.max = max;
            this.step = step;
            this.label = label;
            this.onChange = onChange;
            this.updateMessage();
        }

        private double actualValue() {
            double raw = min + this.value * (max - min);
            if (step > 0) {
                raw = Math.round(raw / step) * step;
            }
            return Math.max(min, Math.min(max, raw));
        }

        @Override
        protected void updateMessage() {
            this.setMessage(label.apply(actualValue()));
        }

        @Override
        protected void applyValue() {
            onChange.accept(actualValue());
        }
    }
}
