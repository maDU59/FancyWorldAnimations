package fr.madu59.fwa.rendering;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;

public class RenderHelper {

    private static final BlockModelLighter lighter = new BlockModelLighter();
    private static final CardinalLighting cardinalLighting = Minecraft.getInstance().level.cardinalLighting();

    public static void renderQuad(VertexConsumer buffer, Pose pose, BakedQuad bakedQuad, float a, float r, float g, float b, int light){
        renderQuad(buffer, pose, bakedQuad, a, r, g, b, light, true);
    }

    public static void renderQuad(VertexConsumer buffer, Pose pose, BakedQuad bakedQuad, float a, float r, float g, float b, int light, boolean isShaded){
        Float shade = 1f;
        if(isShaded){
            float bottomShade = cardinalLighting.byFace(Direction.DOWN);
            float topShade = cardinalLighting.byFace(Direction.UP);
            float ZShade = cardinalLighting.byFace(Direction.NORTH);
            float XShade = cardinalLighting.byFace(Direction.EAST);
            Vector3f normal = new Vector3f(bakedQuad.direction().getUnitVec3f());
            normal.mul(pose.normal());
            normal.normalize();  
            float nx2 = normal.x() * normal.x();
            float ny2 = normal.y() * normal.y();
            float nz2 = normal.z() * normal.z();

            float yShade = normal.y() > 0 ? topShade : bottomShade;
            shade =  (nx2 * XShade) + (ny2 * yShade) + (nz2 * ZShade);
        }

        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setLightCoords(light);
        quadInstance.setColor(ARGB.colorFromFloat(a,r*shade,g*shade,b*shade));
        buffer.putBakedQuad(pose, bakedQuad, quadInstance);
    }
}
