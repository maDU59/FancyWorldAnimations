package fr.madu59.fwa.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

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

    public static void endBatch(MultiBufferSource bufferSource){
        if(bufferSource instanceof BufferSource bs){
            bs.endBatch();
        }
    }

    public static BlockStateModel getInvisibleModel(BlockStateModel originalModel){
        return (BlockStateModel)(originalModel != null && originalModel instanceof InvisibleModel ? originalModel : new InvisibleModel(originalModel));
    }

    private static float scaleCoordinate(float value) {
        return 0.5F + (value - 0.5F) * INVISIBLE_SCALE_VALUE;
    }

   private static Vector3fc scalePosition(Vector3fc position) {
        return new Vector3f(scaleCoordinate(position.x()), scaleCoordinate(position.y()), scaleCoordinate(position.z()));
    }

    private static BakedQuad scaleQuad(BakedQuad quad) {
        return new BakedQuad(scalePosition(quad.position0()), scalePosition(quad.position1()), scalePosition(quad.position2()), scalePosition(quad.position3()), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission());
    }

    private static final class InvisibleModel implements BlockStateModel{
        private final BlockStateModel model;

        private InvisibleModel(BlockStateModel originalModel) {
            this.model = originalModel;
        }

        public void collectParts(RandomSource random, List<BlockModelPart> parts) {
            int start = parts.size();
            this.model.collectParts(random, parts);

            for(int i = start; i < parts.size(); ++i) {
                BlockModelPart part = (BlockModelPart)parts.get(i);
                if (!(part instanceof InvisibleBlockStateModelPart)) {
                    parts.set(i, new InvisibleBlockStateModelPart(part));
                }
            }

        }

        public TextureAtlasSprite particleIcon() {
            return this.model.particleIcon();
        }
    }

    private static final class InvisibleBlockStateModelPart implements BlockModelPart {
        private final BlockModelPart part;
        private final Map<Direction, List<BakedQuad>> directionalCache = new HashMap<Direction, List<BakedQuad>>();

        private InvisibleBlockStateModelPart(BlockModelPart originalPart) {
            this.part = originalPart;
        }

        public List<BakedQuad> getQuads(Direction direction) {
            return this.directionalCache.computeIfAbsent(direction, (key) -> scaleQuads(this.part.getQuads(key)));
        }

        public boolean useAmbientOcclusion() {
            return false;
        }

        public TextureAtlasSprite particleIcon() {
            return this.part.particleIcon();
        }

        private static List<BakedQuad> scaleQuads(List<BakedQuad> quads) {
            if (quads.isEmpty()) {
                return quads;
            } else {
                List<BakedQuad> scaledQuads = new ArrayList<BakedQuad>(quads.size());

                for(BakedQuad quad : quads) {
                    scaledQuads.add(scaleQuad(quad));
                }

                return scaledQuads;
            }
        }
   }
}
