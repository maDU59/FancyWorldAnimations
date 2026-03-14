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
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RenderHelper {

    public static void renderModel(VertexConsumer buffer, Pose pose, BakedModel model, float a, float r, float g, float b, int light, RandomSource random, BlockState blockState){
        renderQuads(buffer, pose, model.getQuads(blockState, null, random), a, r, g, b, light);
        for(Direction dir : Direction.values()){
            renderQuads(buffer, pose, model.getQuads(blockState, dir, random), a, r, g, b, light);
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
            ClientLevel level = Minecraft.getInstance().level;
            float bottomShade = level.getShade(Direction.DOWN, true);
            float topShade = level.getShade(Direction.UP, true);
            float ZShade = level.getShade(Direction.NORTH, true);
            float XShade = level.getShade(Direction.EAST, true);
            Vector3f normal = bakedQuad.getDirection().step();
            normal.mul(pose.normal());
            normal.normalize();  
            float nx2 = normal.x() * normal.x();
            float ny2 = normal.y() * normal.y();
            float nz2 = normal.z() * normal.z();

            float yShade = normal.y() > 0 ? topShade : bottomShade;
            shade =  (nx2 * XShade) + (ny2 * yShade) + (nz2 * ZShade);
        }

        buffer.putBulkData(pose, bakedQuad, r * shade, g * shade, b * shade, light, OverlayTexture.NO_OVERLAY);
    }
}
