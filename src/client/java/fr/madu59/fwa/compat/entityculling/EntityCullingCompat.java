package fr.madu59.fwa.compat.entityculling;

import dev.tr7zw.entityculling.versionless.api.EntityCullingAPIVersionless;
import dev.tr7zw.entityculling.versionless.occlusionculling.util.Vec3d;
import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ConcurrentHashMap;

public class EntityCullingCompat {
    public static void init() {
        EntityCullingAPIVersionless.getCULLING_EVENT().register((inst) -> {
            Vec3d aabbMin = new Vec3d(0, 0, 0);
            Vec3d aabbMax = new Vec3d(0, 0, 0);
            for (ConcurrentHashMap<BlockPos, Animation> chunkAnimations : FancyWorldAnimationsClient.animations.animations.values()) {
                for (Animation animation : chunkAnimations.values()) {
                    if(!animation.hasInfiniteAnimation()) continue;
                    if(!animation.isRendering()) continue;
                    AABB boundingBox = animation.getBoundingBox();
                    aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
                    aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
                    boolean visible = inst.isAABBVisible(aabbMin, aabbMax);
                    animation.setIsOcclusionCulled(!visible);
                }
            }
        });
    }

}
