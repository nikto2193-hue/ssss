package dev.invisibleesp.mixin;

import dev.invisibleesp.esp.EspManager;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla never draws the name tag of a player that is invisible to the viewer. This forces it for
 * highlighted players, and makes sure the render state asks for the outline layer.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "hasLabel(Lnet/minecraft/entity/LivingEntity;D)Z", at = @At("HEAD"), cancellable = true)
    private void invisibleesp$forceLabel(LivingEntity entity, double squaredDistanceToCamera,
                                         CallbackInfoReturnable<Boolean> cir) {
        if (EspManager.shouldShowName(entity)) {
            cir.setReturnValue(true);
        }
    }

    // Redundant with MinecraftClientMixin on purpose: guarantees the outline render layer is chosen.
    @Inject(
            method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;"
                    + "Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL"),
            require = 0)
    private void invisibleesp$forceOutlineState(LivingEntity entity, LivingEntityRenderState state,
                                                float tickDelta, CallbackInfo ci) {
        if (EspManager.isTarget(entity)) {
            state.hasOutline = true;
        }
    }
}
