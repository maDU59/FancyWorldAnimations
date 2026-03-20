package fr.madu59.fwa.rendering;

import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.CardinalLighting;

public class RenderHelper {

    private static final CardinalLighting cardinalLighting = Minecraft.getInstance().level.cardinalLighting();

    public static void renderModel(VertexConsumer buffer, Pose pose, List<BlockStateModelPart> parts, float a, float r, float g, float b, int light){
        for (BlockStateModelPart part : parts){
            renderQuads(buffer, pose, part.getQuads(null), a, r, g, b, light);
            for(Direction dir : Direction.values()){
                renderQuads(buffer, pose, part.getQuads(dir), a, r, g, b, light);
            }
        }
    }

    public static void renderQuads(VertexConsumer buffer, Pose pose, List<BakedQuad> bakedQuads, float a, float r, float g, float b, int light){
        for (BakedQuad bakedQuad : bakedQuads){
            renderQuad(buffer, pose, bakedQuad, a, r, g, b, light);
        }
    }

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
