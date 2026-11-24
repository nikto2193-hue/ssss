package dev.invisibleesp.esp;

import dev.invisibleesp.config.EspConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps the set of players that must currently be highlighted.
 *
 * <p>The set is recalculated once per client tick, so the highlight is stable between frames
 * (no flicker) and the render hooks only perform a cheap lookup.
 */
public final class EspManager {
    private static volatile Set<Integer> targets = Collections.emptySet();

    private EspManager() {
    }

    public static void tick(MinecraftClient client) {
        EspConfig config = EspConfig.get();
        ClientWorld world = client.world;
        ClientPlayerEntity self = client.player;
        if (!config.enabled || world == null || self == null) {
            targets = Collections.emptySet();
            return;
        }

        double maxDistanceSq = (double) config.renderDistance * config.renderDistance;
        Set<Integer> found = new HashSet<>();
        for (AbstractClientPlayerEntity other : world.getPlayers()) {
            if (other == self || !InvisibleDetector.isInvisible(other)) {
                continue;
            }
            if (other.squaredDistanceTo(self) > maxDistanceSq) {
                continue;
            }
            if (!config.throughWalls && !hasLineOfSight(world, self, other)) {
                continue;
            }
            found.add(other.getId());
        }
        targets = found;
    }

    /** True if this entity is an invisible player that is currently highlighted. */
    public static boolean isTarget(Entity entity) {
        if (!(entity instanceof PlayerEntity) || !entity.getWorld().isClient()) {
            return false;
        }
        return EspConfig.get().enabled && targets.contains(entity.getId());
    }

    /** True if a name tag must be forced for this entity. */
    public static boolean shouldShowName(Entity entity) {
        return EspConfig.get().showName && isTarget(entity);
    }

    /** Outline colour as 0xRRGGBB. */
    public static int outlineColor() {
        return EspConfig.get().outlineRgb();
    }

    private static boolean hasLineOfSight(ClientWorld world, ClientPlayerEntity viewer, PlayerEntity target) {
        Vec3d from = viewer.getEyePos();
        return canSee(world, viewer, from, target.getEyePos())
                || canSee(world, viewer, from, target.getBoundingBox().getCenter());
    }

    private static boolean canSee(ClientWorld world, Entity viewer, Vec3d from, Vec3d to) {
        RaycastContext context = new RaycastContext(
                from, to, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, viewer);
        return world.raycast(context).getType() == HitResult.Type.MISS;
    }
}
