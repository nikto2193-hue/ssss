package dev.invisibleesp.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import dev.invisibleesp.InvisibleEspClient;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * User settings, stored as JSON in {@code config/invisibleesp.json}.
 */
public final class EspConfig {
    public static final int MIN_DISTANCE = 8;
    public static final int MAX_DISTANCE = 128;
    public static final int MIN_BRIGHTNESS = 10;
    public static final int MAX_BRIGHTNESS = 100;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static EspConfig instance = new EspConfig();

    /** Master switch (hotkey H). */
    public boolean enabled = true;
    /** Show the name tag above highlighted players. */
    public boolean showName = true;
    /** Draw the outline even when blocks are in the way. */
    public boolean throughWalls = true;
    /** Outline colour, 0-255 per channel. Default: bright red. */
    public int red = 255;
    public int green = 0;
    public int blue = 0;
    /** Maximum distance in blocks at which invisible players are highlighted. */
    public int renderDistance = 64;
    /** Outline brightness in percent (scales the colour). */
    public int brightness = 100;

    public static EspConfig get() {
        return instance;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("invisibleesp.json");
    }

    public static void load() {
        Path path = configPath();
        EspConfig loaded = null;
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                loaded = GSON.fromJson(reader, EspConfig.class);
            } catch (IOException | JsonParseException e) {
                InvisibleEspClient.LOGGER.warn("Could not read {}, using defaults", path, e);
            }
        }
        instance = loaded != null ? loaded : new EspConfig();
        instance.sanitize();
        save();
    }

    public static void save() {
        instance.sanitize();
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(instance), StandardCharsets.UTF_8);
        } catch (IOException e) {
            InvisibleEspClient.LOGGER.warn("Could not write {}", path, e);
        }
    }

    /** Final outline colour as 0xRRGGBB with brightness applied. */
    public int outlineRgb() {
        int r = red * brightness / 100;
        int g = green * brightness / 100;
        int b = blue * brightness / 100;
        return (r << 16) | (g << 8) | b;
    }

    private void sanitize() {
        red = clamp(red, 0, 255);
        green = clamp(green, 0, 255);
        blue = clamp(blue, 0, 255);
        renderDistance = clamp(renderDistance, MIN_DISTANCE, MAX_DISTANCE);
        brightness = clamp(brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
