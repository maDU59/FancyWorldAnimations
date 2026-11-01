package fr.madu59.fwa.mixin.b3d.posestack;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PoseStack.Pose.class)
public interface PoseConstructorInvoker {

    @Invoker("<init>")
    static PoseStack.Pose fwa$create(Matrix4f pose, Matrix3f normal) {
        throw new AssertionError();
    }
}
