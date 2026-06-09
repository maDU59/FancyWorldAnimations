package fr.madu59.fwa.mixin.entityculling;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.logisticscraft.occlusionculling.OcclusionCullingInstance;
import com.logisticscraft.occlusionculling.util.Vec3d;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.anims.Animation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(targets = "dev.tr7zw.entityculling.CullTask", remap = false)
public abstract class CullTaskMixin {

    private Vec3d aabbMin = new Vec3d(0, 0, 0);
    private Vec3d aabbMax = new Vec3d(0, 0, 0);

    @Shadow
    private OcclusionCullingInstance culling;

    @Shadow
    private int hitboxLimit;

    @Inject(method = "cullEntities", at = @At("TAIL"), require = 0, remap = false)
    public void fwa$cullAnimations(CallbackInfo ci, @Local(argsOnly = true) Vec3 camPos, @Local(argsOnly = true) Vec3d camera){
        fwa$cullAnimations(camPos, camera);
    }

    @Unique
    public void fwa$cullAnimations(Vec3 camPos, Vec3d camera){
        for (Animation animation : FancyWorldAnimationsClient.animations.animations.values()){
            if(!animation.hasInfiniteAnimation()) continue;
            AABB boundingBox = animation.getBoundingBox();
            if (boundingBox == null || boundingBox.getXsize() > hitboxLimit || boundingBox.getYsize() > hitboxLimit || boundingBox.getZsize() > hitboxLimit) {
                animation.setIsOcclusionCulled(false);
                continue;
            }
            aabbMin.set(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
            aabbMax.set(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
            boolean visible = culling.isAABBVisible(aabbMin, aabbMax, camera);
            animation.setIsOcclusionCulled(!visible);
        }
    }
}
