package fr.madu59.fwa.rendering;

import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;

public class RenderHelper {

    private static MultiBufferSource bufferSource;
    private static float bottomShade = 0;
    private static float topShade = 0;
    private static float ZShade = 0;
    private static float XShade = 0;

    public static void prepareFrame(MultiBufferSource source, boolean isShadow){
        if(!isShadow){
            ClientLevel level = Minecraft.getInstance().level;
        bottomShade = level.getShade(Direction.DOWN, true);
        topShade = level.getShade(Direction.UP, true);
        ZShade = level.getShade(Direction.NORTH, true);
        XShade = level.getShade(Direction.EAST, true);
        }
        bufferSource = source;
    }

    public static VertexConsumer getBuffer(){
        return getBuffer(RenderTypes.cutoutMovingBlock());
    }

    public static VertexConsumer getBuffer(RenderType renderType){
        return bufferSource.getBuffer(renderType);
    }

    public static void renderModel(VertexConsumer buffer, Pose pose, List<BlockModelPart> parts, float a, float r, float g, float b, int light){
        for (BlockModelPart part : parts){
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
            Vector3f normal = new Vector3f(bakedQuad.direction().getUnitVec3f());
            normal.mul(pose.normal());
            normal.normalize();  
            float nx2 = normal.x() * normal.x();
            float ny2 = normal.y() * normal.y();
            float nz2 = normal.z() * normal.z();

            float yShade = normal.y() > 0 ? topShade : bottomShade;
            shade =  (nx2 * XShade) + (ny2 * yShade) + (nz2 * ZShade);
        }

        buffer.putBulkData(pose, bakedQuad, r * shade, g * shade, b * shade, a, light, OverlayTexture.NO_OVERLAY);
    }
}
