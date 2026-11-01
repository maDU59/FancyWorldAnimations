package fr.madu59.fwa.mixin.b3d.posestack;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.madu59.fwa.utils.Backport;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PoseStack.Pose.class)
public abstract class PoseMixin implements Backport.PoseExtension {

    @Shadow
    Matrix4f pose;

    @Shadow
    Matrix3f normal;

    @Override
    public PoseStack.Pose fwa$copy() {
        Matrix4f pose = new Matrix4f(this.pose);
        Matrix3f normal = new Matrix3f(this.normal);

        return PoseConstructorInvoker.fwa$create(pose, normal);
    }
}
