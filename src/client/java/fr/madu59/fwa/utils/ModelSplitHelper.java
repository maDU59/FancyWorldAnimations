package fr.madu59.fwa.utils;

import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class ModelSplitHelper {
    public static final float EPSILON = 0.0001f;

    public static boolean is(float value, float target) {
        return Math.abs(value - target) < EPSILON;
    }

    public static boolean gte(float value, float target) {
        return value > target - EPSILON;
    }

    public static boolean lte(float value, float target) {
        return value < target + EPSILON;
    }

    public static boolean isAxisAligned(Vector3fc normal) {
        return Math.abs(normal.x()) > 1.0f - EPSILON ||
            Math.abs(normal.y()) > 1.0f - EPSILON ||
            Math.abs(normal.z()) > 1.0f - EPSILON;
    }

    public static Vector3f middlePoint(BakedQuad quad){
        Vector3f result = new Vector3f();
    
        result.add(quad.position0());
        result.add(quad.position1());
        result.add(quad.position2());
        result.add(quad.position3());
        
        result.div(4.0f);

        return result;
    }

    public record Lever(List<BakedQuad> baseQuadList, List<BakedQuad> handleQuadList, float pivot){}

    public record FenceGate(List<BakedQuad> postQuadList, List<BakedQuad> leftQuadList, List<BakedQuad> rightQuadList){}

    public static enum SPLIT_METHOD{
        MODEL,
        TEXTURE
    }
}
