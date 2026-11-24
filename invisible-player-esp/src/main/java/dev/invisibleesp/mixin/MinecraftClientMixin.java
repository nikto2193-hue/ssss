package dev.invisibleesp.mixin;

import dev.invisibleesp.esp.EspManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes the vanilla entity-outline pipeline treat highlighted invisible players as "glowing".
 * That pipeline draws its outline into a separate framebuffer which is composited on top of the
 * world, so the contour is visible through blocks.
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "hasOutline(Lnet/minecraft/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    private void invisibleesp$forceOutline(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && EspManager.isTarget(entity)) {
            cir.setReturnValue(true);
        }
    }
}
