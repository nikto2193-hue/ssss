package dev.invisibleesp;

import dev.invisibleesp.config.EspConfig;
import dev.invisibleesp.esp.EspManager;
import dev.invisibleesp.input.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint of the Invisible Player ESP mod.
 */
public final class InvisibleEspClient implements ClientModInitializer {
    public static final String MOD_ID = "invisibleesp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        EspConfig.load();
        KeyBindings.register();
        ClientTickEvents.END_CLIENT_TICK.register(EspManager::tick);
        LOGGER.info("Invisible Player ESP initialised");
    }
}
