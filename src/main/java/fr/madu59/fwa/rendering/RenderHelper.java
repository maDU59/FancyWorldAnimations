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
    private static Vector3f normal = new Vector3f();
    private static boolean shouldShade = true;
    private static final Direction[] DIRECTIONS_WITH_NULL = {
        null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };
    private static final Direction[] DIRECTIONS = Direction.values();

    public static void prepareFrame(AnimationRenderingContext context){
        if(!context.isShadow()){
            ClientLevel level = Minecraft.getInstance().level;
            bottomShade = level.getShade(Direction.DOWN, true);
            topShade = level.getShade(Direction.UP, true);
            ZShade = level.getShade(Direction.NORTH, true);
            XShade = level.getShade(Direction.EAST, true);
        }
        shouldShade = !context.isShadow();
        bufferSource = context.getBufferSource();
    }

    public static VertexConsumer getBuffer(){
        return getBuffer(RenderTypes.cutoutMovingBlock());
    }

    public static VertexConsumer getBuffer(RenderType renderType){
        return bufferSource.getBuffer(renderType);
    }

    public static void renderModel(VertexConsumer buffer, Pose pose, List<BlockModelPart> parts, float a, float r, float g, float b, int light){
        for (int i = 0; i < parts.size(); i++){
            for (int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
                renderQuads(buffer, pose, parts.get(i).getQuads(DIRECTIONS_WITH_NULL[j]), a, r, g, b, light);
            }
        }
    }

    // Does not seem to make any difference
    // public static void renderModelVisibleOnly(VertexConsumer buffer, Pose pose, List<BlockStateModelPart> parts, float a, float r, float g, float b, int light, BlockPos pos){
    //     Vec3 dirV = camPos.subtract(pos.getCenter());
    //     for (BlockStateModelPart part : parts){
    //         renderQuads(buffer, pose, part.getQuads(null), a, r, g, b, light);
    //         for(Direction dir : DIRECTIONS){
    //             if(dirV.dot(dir.getUnitVec3()) >= 0) renderQuads(buffer, pose, part.getQuads(dir), a, r, g, b, light);
    //         }
    //     }
    // }

    public static void renderQuads(VertexConsumer buffer, Pose pose, List<BakedQuad> bakedQuads, float a, float r, float g, float b, int light){
        for (int i = 0; i < bakedQuads.size(); i++){
            renderQuad(buffer, pose, bakedQuads.get(i), a, r, g, b, light, shouldShade);
        }
    }

    public static void renderQuad(VertexConsumer buffer, Pose pose, BakedQuad bakedQuad, float a, float r, float g, float b, int light){
        renderQuad(buffer, pose, bakedQuad, a, r, g, b, light, shouldShade);
    }

    public static void renderQuad(VertexConsumer buffer, Pose pose, BakedQuad bakedQuad, float a, float r, float g, float b, int light, boolean isShaded){
        Float shade = 1f;
        if(isShaded){
            normal.set(bakedQuad.direction().getUnitVec3f());
            normal.mul(pose.normal());
            normal.normalize(); // Might not be needed
            float nx2 = normal.x() * normal.x();
            float ny2 = normal.y() * normal.y();
            float nz2 = normal.z() * normal.z();

            float yShade = normal.y() > 0 ? topShade : bottomShade;
            shade =  (nx2 * XShade) + (ny2 * yShade) + (nz2 * ZShade);
        }

        buffer.putBulkData(pose, bakedQuad, r * shade, g * shade, b * shade, a, light, OverlayTexture.NO_OVERLAY);
    }
}
