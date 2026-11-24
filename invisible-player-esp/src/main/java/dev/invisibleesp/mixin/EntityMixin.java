package dev.invisibleesp.mixin;

import dev.invisibleesp.esp.EspManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The vanilla outline takes its colour from the entity's team colour. For highlighted invisible
 * players this returns the colour chosen in the settings menu instead.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void invisibleesp$overrideOutlineColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (EspManager.isTarget(self)) {
            cir.setReturnValue(EspManager.outlineColor());
        }
    }
}
