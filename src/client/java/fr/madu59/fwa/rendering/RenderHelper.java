package fr.madu59.fwa.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import fr.madu59.fwa.utils.Backport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
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
    private static boolean shouldShade = true;
    private static final float INVISIBLE_SCALE_VALUE = 0.0001f;
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
        return getBuffer(RenderType.cutoutMipped());
    }

    public static VertexConsumer getBuffer(RenderType renderType){
        return bufferSource.getBuffer(renderType);
    }

    public static void renderModel(VertexConsumer buffer, Pose pose, BakedModel model, float a, float r, float g, float b, int light, RandomSource random, BlockState blockState){
        for (int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
            renderQuads(buffer, pose, model.getQuads(blockState, DIRECTIONS_WITH_NULL[j], random), a, r, g, b, light);
        }
    }

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
            normal.set(bakedQuad.getDirection().step());
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

    public static void endBatch(MultiBufferSource bufferSource){
        if(bufferSource instanceof BufferSource bs){
            bs.endBatch();
        }
    }

    public static BakedModel getInvisibleModel(BakedModel originalModel){
        return (BakedModel)(originalModel != null && originalModel instanceof InvisibleModel ? originalModel : new InvisibleModel(originalModel));
    }

    private static float scaleCoordinate(float value) {
        return 0.5F + (value - 0.5F) * INVISIBLE_SCALE_VALUE;
    }

    private static int[] scaleVertices(int[] vertices) {
        int[] scaledVertices = vertices.clone();
        for (int i = 0; i < vertices.length; i += 8) {
            float x = Float.intBitsToFloat(vertices[i]);
            float y = Float.intBitsToFloat(vertices[i + 1]);
            float z = Float.intBitsToFloat(vertices[i + 2]);

            scaledVertices[i] = Float.floatToIntBits(scaleCoordinate(x));
            scaledVertices[i + 1] = Float.floatToIntBits(scaleCoordinate(y));
            scaledVertices[i + 2] = Float.floatToIntBits(scaleCoordinate(z));
        }
        return scaledVertices;
    }

    private static BakedQuad scaleQuad(BakedQuad quad) {
        return new BakedQuad(scaleVertices(quad.getVertices()), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade(), quad.getLightEmission());
    }

    private static List<BakedQuad> scaleQuads(List<BakedQuad> quads) {
        if (quads.isEmpty()) {
            return quads;
        } else {
            List<BakedQuad> scaled = new ArrayList<BakedQuad>(quads.size());

            for(BakedQuad quad : quads) {
                scaled.add(scaleQuad(quad));
            }

            return List.copyOf(scaled);
        }
    }

    private static final class InvisibleModel implements BakedModel{
        private final BakedModel model;

        private InvisibleModel(BakedModel originalModel) {
            this.model = originalModel;
        }
        
        public TextureAtlasSprite getParticleIcon() {
            return model.getParticleIcon();
        }

        public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource randomSource) {
            return scaleQuads(model.getQuads(state, direction, randomSource));
        }

        public ItemTransforms getTransforms() {
            return model.getTransforms();
        }

        public boolean isGui3d() {
            return false;
        }

        public boolean useAmbientOcclusion() {
            return false;
        }

        public boolean usesBlockLight() {
            return false;
        }

        public boolean isCustomRenderer() {
            return this.model.isCustomRenderer();
        }
    }
}
