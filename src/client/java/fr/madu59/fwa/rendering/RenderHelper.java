package fr.madu59.fwa.rendering;

import java.util.List;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RenderHelper {

    private static MultiBufferSource bufferSource;
    private static float bottomShade = 0;
    private static float topShade = 0;
    private static float ZShade = 0;
    private static float XShade = 0;
    private static Vector3f normal = new Vector3f();

    public static void prepareFrame(AnimationRenderingContext context){
        if(!context.isShadow()){
            ClientLevel level = Minecraft.getInstance().level;
            bottomShade = level.getShade(Direction.DOWN, true);
            topShade = level.getShade(Direction.UP, true);
            ZShade = level.getShade(Direction.NORTH, true);
            XShade = level.getShade(Direction.EAST, true);
        }
        bufferSource = context.getBufferSource();
    }

    public static VertexConsumer getBuffer(){
        return getBuffer(RenderType.cutoutMipped());
    }

    public static VertexConsumer getBuffer(RenderType renderType){
        return bufferSource.getBuffer(renderType);
    }

    public static void renderModel(VertexConsumer buffer, Pose pose, BakedModel model, float a, float r, float g, float b, int light, RandomSource random, BlockState blockState){
        renderQuads(buffer, pose, model.getQuads(blockState, null, random), a, r, g, b, light);
        for(Direction dir : Direction.values()){
            renderQuads(buffer, pose, model.getQuads(blockState, dir, random), a, r, g, b, light);
        }
    }

    // Does not seem to make any difference
    // public static void renderModelVisibleOnly(VertexConsumer buffer, Pose pose, List<BlockStateModelPart> parts, float a, float r, float g, float b, int light, BlockPos pos){
    //     Vec3 dirV = camPos.subtract(pos.getCenter());
    //     for (BlockStateModelPart part : parts){
    //         renderQuads(buffer, pose, part.getQuads(null), a, r, g, b, light);
    //         for(Direction dir : Direction.values()){
    //             if(dirV.dot(dir.getUnitVec3()) >= 0) renderQuads(buffer, pose, part.getQuads(dir), a, r, g, b, light);
    //         }
    //     }
    // }

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
            normal.set(bakedQuad.getDirection().step());
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
