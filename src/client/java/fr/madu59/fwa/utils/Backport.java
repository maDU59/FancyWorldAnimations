package fr.madu59.fwa.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class Backport {
    public static Vector3f getPos(BakedQuad quad, int vertexIndex) {
        int[] vertexData = quad.getVertices();
        int offset = vertexIndex * 8; 
        
        float x = Float.intBitsToFloat(vertexData[offset]);
        float y = Float.intBitsToFloat(vertexData[offset + 1]);
        float z = Float.intBitsToFloat(vertexData[offset + 2]);
        
        return new Vector3f(x, y, z);
    }

    public static <T extends Comparable<T>> T getValueOrElse(BlockState state, Property<T> property, T defaultValue) {
        return state.hasProperty(property) ? state.getValue(property) : defaultValue;
    }

    public static PoseStack.Pose copyPose(PoseStack poseStack) {
        return copyPose(poseStack.last());
    }

    public static PoseStack.Pose copyPose(PoseStack.Pose pose) {
        return ((PoseExtension) (Object) pose).fwa$copy();
    }

    public interface PoseExtension {
        PoseStack.Pose fwa$copy();
    }
}
