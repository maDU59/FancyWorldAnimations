package fr.madu59.fwa.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.compat.ModCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
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

    private static final Matrix4f SHRINK_MATRIX = new Matrix4f()
    .translation(0.5f, 0.5f, 0.5f)
    .scale(0.0002f)
    .translate(-0.5f, -0.5f, -0.5f);

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

    public static void renderModel(PoseStack.Pose pose, List<BlockModelPart> parts, float a, float r, float g, float b, int light, AnimationBlockData blockData){
        for (int i = 0; i < parts.size(); i++){
            BlockModelPart part = parts.get(i);
            for (int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
                List<BakedQuad> quads = part.getQuads(DIRECTIONS_WITH_NULL[j]);
                int n = quads.size();
                if (n == 0) continue;
                for (int k = 0; k < n; k++){
                    renderQuad(pose, quads.get(k), a, r, g, b, light, blockData);
                }
            }
        }
    }

    public static void renderQuads(PoseStack.Pose pose, BakedQuad[] quads, float a, float r, float g, float b, int light, AnimationBlockData blockData){
        for (int i = 0; i < quads.length; i++){
            renderQuad(pose, quads[i], a, r, g, b, light, blockData);
        }
    }

    public static void renderQuad(PoseStack.Pose pose, BakedQuad bakedQuad, float a, float r, float g, float b, int light, AnimationBlockData blockData){
        Vector3fc dir = bakedQuad.direction().getUnitVec3f();
        float shade = blockData.isShaded()? getShade(dir.x(), dir.y(), dir.z(), pose) : 1.0f;
        int color = ARGB.colorFromFloat(a,r*shade,g*shade,b*shade);

        RenderType renderType;
        if(a < 1.0f){
            renderType = RenderTypes.translucentMovingBlock();
        } else {
            renderType = switch (ItemBlockRenderTypes.getChunkRenderType(blockData.getBlockState())) {
                case ChunkSectionLayer.TRANSLUCENT -> RenderTypes.translucentMovingBlock();
                case ChunkSectionLayer.CUTOUT -> RenderTypes.cutoutMovingBlock();
                case ChunkSectionLayer.SOLID -> RenderTypes.solidMovingBlock();
                default -> RenderTypes.cutoutMovingBlock();
            };
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

                Vector4f colors = ARGB.vector4fFromARGB32(quad.color());
                consumer.putBulkData(quad.pose(), quad.bakedQuad(), colors.x, colors.y, colors.z, colors.w, quad.light(), OverlayTexture.NO_OVERLAY);
            }
            ModCompat.endBlock(consumer);
        }
        buffers.clear();
    }

    public static BakedQuad[] flattenModel(List<BlockModelPart> parts){
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++){
            BlockModelPart part = parts.get(i);
            for (int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
                quads.addAll(part.getQuads(DIRECTIONS_WITH_NULL[j]));
            }
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

    public static BlockStateModel getInvisibleModel(BlockStateModel originalModel, BlockState state){
        return (originalModel != null && originalModel instanceof InvisibleModel ? originalModel : new InvisibleModel(originalModel));
    }

    private static Vector3fc scaleVertex(Vector3fc position) {
        return SHRINK_MATRIX.transformPosition(new Vector3f(position));
    }

    private static BakedQuad scaleQuad(BakedQuad quad) {
        return new BakedQuad(scaleVertex(quad.position0()), scaleVertex(quad.position1()), scaleVertex(quad.position2()), scaleVertex(quad.position3()), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission());
    }

    private static class InvisibleModel implements BlockStateModel{

        private final BlockStateModel model;

        public InvisibleModel(BlockStateModel model) {
            this.model = model;
        }

        @Override
        public void collectParts(RandomSource random, List<BlockModelPart> parts) {
            int start = parts.size();
            this.model.collectParts(random, parts);
            int end = parts.size();

            for(int i = start; i < end; i++) {
                BlockModelPart part = parts.get(i);
                if (!(part instanceof InvisibleBlockModelPart)) {
                    parts.set(i, new InvisibleBlockModelPart(part));
                }
            }

        }

        public TextureAtlasSprite particleIcon() {
            return this.model.particleIcon();
        }
    }

    private static final class InvisibleBlockModelPart implements BlockModelPart {
        private final BlockModelPart part;
        private final Map<Direction, List<BakedQuad>> directions = new HashMap<Direction, List<BakedQuad>>();

        private InvisibleBlockModelPart(BlockModelPart originalPart) {
            this.part = originalPart;
        }

        @Override
        public List<BakedQuad> getQuads(Direction direction) {
            return this.directions.computeIfAbsent(direction, (k) -> scaleQuads(this.part.getQuads(k)));
        }

        @Override
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
