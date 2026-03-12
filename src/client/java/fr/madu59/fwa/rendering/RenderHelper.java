package fr.madu59.fwa.rendering;

import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;

public class RenderHelper {

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
            System.out.println(bottomShade);
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
