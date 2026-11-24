package dev.invisibleesp.input;

import dev.invisibleesp.config.EspConfig;
import dev.invisibleesp.gui.EspSettingsScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Registers the mod hotkeys. Both appear in Options -> Controls -> Key Binds and can be rebound.
 */
public final class KeyBindings {
    private static final String CATEGORY = "category.invisibleesp";

    private static KeyBinding openMenu;
    private static KeyBinding toggleEsp;

    private KeyBindings() {
    }

    public static void register() {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.invisibleesp.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        toggleEsp = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.invisibleesp.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_H, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(KeyBindings::handle);
    }

    /** The "open menu" binding, used by the settings screen to close itself with the same key. */
    public static KeyBinding openMenuKey() {
        return openMenu;
    }

    private static void handle(MinecraftClient client) {
        while (toggleEsp.wasPressed()) {
            EspConfig config = EspConfig.get();
            config.enabled = !config.enabled;
            EspConfig.save();
            if (client.player != null) {
                client.player.sendMessage(Text.translatable(
                        config.enabled ? "message.invisibleesp.enabled" : "message.invisibleesp.disabled"), true);
            }
        }
        while (openMenu.wasPressed()) {
            if (client.currentScreen == null && client.world != null) {
                client.setScreen(new EspSettingsScreen(null));
            }
        }
    }
}
