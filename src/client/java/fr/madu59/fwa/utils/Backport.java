package fr.madu59.fwa.utils;

import org.joml.Vector3f;

import net.minecraft.client.renderer.block.model.BakedQuad;

public class Backport {
    public static Vector3f getPos(BakedQuad quad, int vertexIndex) {
        int[] vertexData = quad.vertices();
        int offset = vertexIndex * 8; 
        
        float x = Float.intBitsToFloat(vertexData[offset]);
        float y = Float.intBitsToFloat(vertexData[offset + 1]);
        float z = Float.intBitsToFloat(vertexData[offset + 2]);
        
        return new Vector3f(x, y, z);
    }
}
