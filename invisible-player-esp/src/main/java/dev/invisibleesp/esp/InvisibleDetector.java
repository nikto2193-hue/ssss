package dev.invisibleesp.esp;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Decides whether a player counts as "invisible".
 *
 * <p>A vanilla server only sends potion effects to the affected player itself. Other clients learn
 * about invisibility through the entity's "invisible" flag, which the server sets whenever the
 * Invisibility effect is active. For that reason the flag is checked in addition to the effect:
 * the effect check covers the local data when it is available, the flag covers other players.
 */
public final class InvisibleDetector {
    private InvisibleDetector() {
    }

    public static boolean isInvisible(PlayerEntity player) {
        if (player.isSpectator()) {
            return false;
        }
        return player.hasStatusEffect(StatusEffects.INVISIBILITY) || player.isInvisible();
    }
}
