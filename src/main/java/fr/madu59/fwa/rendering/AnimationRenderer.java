package fr.madu59.fwa.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.util.FastColor;
import org.joml.Vector3f;
import java.util.Map;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import org.joml.Matrix3fc;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.compat.ModCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class AnimationRenderer {

    private static float bottomShade = 0;
    private static float topShade = 0;
    private static float ZShade = 0;
    private static float XShade = 0;
    private static boolean shouldShade = true;
    private static final Direction[] DIRECTIONS_WITH_NULL = {
        null, Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST
    };
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Map<RenderType, List<AnimationQuad>> buffers = new HashMap<>();
    private static MultiBufferSource bufferSource;

    private static final float INVISIBLE_SCALE_VALUE = 0.0002f;

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
        buffers.clear();
    }

    public static void renderModel(PoseStack.Pose pose, BakedModel model, float a, float r, float g, float b, int light, AnimationBlockData blockData, RandomSource random, BlockState blockState){
        for (int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
            List<BakedQuad> quads = model.getQuads(blockState, DIRECTIONS_WITH_NULL[j], random);
            int n = quads.size();
            if (n == 0) continue;
            for (int k = 0; k < n; k++){
                renderQuad(pose, quads.get(k), a, r, g, b, light, blockData);
            }
        }
    }

    public static void renderQuads(PoseStack.Pose pose, BakedQuad[] quads, float a, float r, float g, float b, int light, AnimationBlockData blockData){
        for (int i = 0; i < quads.length; i++){
            renderQuad(pose, quads[i], a, r, g, b, light, blockData);
        }
    }

    public static void renderQuad(PoseStack.Pose pose, BakedQuad bakedQuad, float a, float r, float g, float b, int light, AnimationBlockData blockData){
        Vector3f dir = bakedQuad.getDirection().step();
        float shade = blockData.isShaded()? getShade(dir.x(), dir.y(), dir.z(), pose) : 1.0f;
        int color = FastColor.ARGB32.colorFromFloat(a,r*shade,g*shade,b*shade);

        RenderType renderType;
        if(a < 1.0f){
            renderType = RenderType.translucentMovingBlock();
        } else {
            renderType = ItemBlockRenderTypes.getMovingBlockRenderType(blockData.getBlockState());
        }

        buffers.computeIfAbsent(renderType, k -> new ArrayList<>()).add(new AnimationQuad(pose, bakedQuad, light, color, blockData));
    }

    public static void renderBuffers(){
        for(Map.Entry<RenderType, List<AnimationQuad>> entry : buffers.entrySet()){
            RenderType renderType = entry.getKey();
            VertexConsumer consumer = bufferSource.getBuffer(renderType);

            AnimationBlockData lastBlockData = null;

            for(AnimationQuad quad : entry.getValue()){
                if(quad.blockData() != lastBlockData){
                    if(lastBlockData != null){
                        ModCompat.endBlock(consumer);
                    }
                    lastBlockData = quad.blockData();
                    ModCompat.beginBlock(consumer, lastBlockData);
                }

                consumer.putBulkData(quad.pose(), quad.bakedQuad(), FastColor.ARGB32.red(quad.color()) / 255.0f, FastColor.ARGB32.green(quad.color()) / 255.0f, FastColor.ARGB32.blue(quad.color()) / 255.0f, FastColor.ARGB32.alpha(quad.color()) / 255.0f, quad.light(), OverlayTexture.NO_OVERLAY);;
            }
            ModCompat.endBlock(consumer);
        }
        buffers.clear();
    }

    public static BakedQuad[] flattenModel(BakedModel model, BlockState state, RandomSource random){
        List<BakedQuad> quads = new ArrayList<>();
        for(int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
            quads.addAll(model.getQuads(state, DIRECTIONS_WITH_NULL[j], random));
        }
        return quads.toArray(new BakedQuad[0]);
    }

    public static float getShade(float nx, float ny, float nz, PoseStack.Pose pose){
        if (!shouldShade) return 1f;
        Matrix3fc m = pose.normal();
        float x = m.m00() * nx + m.m10() * ny + m.m20() * nz;
        float y = m.m01() * nx + m.m11() * ny + m.m21() * nz;
        float z = m.m02() * nx + m.m12() * ny + m.m22() * nz;
        float x2 = x * x, y2 = y * y, z2 = z * z;
        float yShade = y > 0f ? topShade : bottomShade;
        return (x2 * XShade + y2 * yShade + z2 * ZShade) / (x2 + y2 + z2);
    }

    public static void endBatch(MultiBufferSource bufferSource){
        if(bufferSource instanceof BufferSource bs){
            bs.endBatch();
        }
    }

    public static BakedModel getInvisibleModel(BakedModel originalModel, BlockState state){
        return (originalModel != null && originalModel instanceof InvisibleModel ? originalModel : new InvisibleModel(originalModel));
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
        return new BakedQuad(scaleVertices(quad.getVertices()), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
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
            return this.model.getParticleIcon();
        }

        public List<BakedQuad> getQuads(BlockState state, Direction direction, RandomSource randomSource) {
            return scaleQuads(this.model.getQuads(state, direction, randomSource));
        }

        public ItemTransforms getTransforms() {
            return this.model.getTransforms();
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

        @Override
        public ItemOverrides getOverrides() {
            return this.model.getOverrides();
        }
    }
}
