package fr.madu59.fwa.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack.Pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.compat.ModCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;

public class AnimationRenderer {

    private static SubmitNodeCollector collector;
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

    private static final Matrix4f SHRINK_MATRIX = new Matrix4f()
    .translation(0.5f, 0.5f, 0.5f)
    .scale(0.0002f)
    .translate(-0.5f, -0.5f, -0.5f);

    public static void prepareFrame(AnimationRenderingContext context){
        if(!context.isShadow()){
            CardinalLighting cardinalLighting = Minecraft.getInstance().level.cardinalLighting();
            bottomShade = cardinalLighting.byFace(Direction.DOWN);
            topShade = cardinalLighting.byFace(Direction.UP);
            ZShade = cardinalLighting.byFace(Direction.NORTH);
            XShade = cardinalLighting.byFace(Direction.EAST);
        }
        shouldShade = !context.isShadow();
        collector = context.getSubmitNodeCollector();
        buffers.clear();
    }

    public static void renderModel(PoseStack.Pose pose, List<BlockStateModelPart> parts, float a, float r, float g, float b, int light, AnimationBlockData blockData){
        for (int i = 0; i < parts.size(); i++){
            BlockStateModelPart part = parts.get(i);
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
            renderType = switch (bakedQuad.materialInfo().layer()) {
                case ChunkSectionLayer.TRANSLUCENT -> RenderTypes.translucentMovingBlock();
                case ChunkSectionLayer.CUTOUT -> RenderTypes.cutoutMovingBlock();
                case ChunkSectionLayer.SOLID -> RenderTypes.solidMovingBlock();
                default -> RenderTypes.cutoutMovingBlock();
            };
        }

        buffers.computeIfAbsent(renderType, k -> new ArrayList<>()).add(new AnimationQuad(pose, bakedQuad, light, color, blockData));
    }

    public static void renderBuffers(){
        PoseStack poseStack = new PoseStack();
        QuadInstance quadInstance = new QuadInstance();
        for(Map.Entry<RenderType, List<AnimationQuad>> entry : buffers.entrySet()){
            RenderType renderType = entry.getKey();
            collector.submitCustomGeometry(
                poseStack,
                renderType,
                (Pose currentPose, VertexConsumer consumer) -> {
                    AnimationBlockData lastBlockData = null;

                    for(AnimationQuad quad : entry.getValue()){
                        if(quad.blockData() != lastBlockData){
                            if(lastBlockData != null){
                                ModCompat.endBlock(consumer);
                            }
                            lastBlockData = quad.blockData();
                            ModCompat.beginBlock(consumer, lastBlockData);
                        }

                        quadInstance.setLightCoords(quad.light());
                        quadInstance.setColor(quad.color());
                        consumer.putBakedQuad(
                            quad.pose(), quad.bakedQuad(), quadInstance
                        );
                    }
                    ModCompat.endBlock(consumer);
                }
            );
        }
        buffers.clear();
    }

    public static BakedQuad[] flattenModel(List<BlockStateModelPart> parts){
        List<BakedQuad> quads = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++){
            BlockStateModelPart part = parts.get(i);
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

    public static BlockStateModel getInvisibleModel(BlockStateModel originalModel, BlockState state){
        return (originalModel != null && originalModel instanceof InvisibleModel ? originalModel : new InvisibleModel(originalModel));
    }

    private static Vector3fc scaleVertex(Vector3fc position) {
        return SHRINK_MATRIX.transformPosition(new Vector3f(position));
    }

    private static BakedQuad scaleQuad(BakedQuad quad) {
        return new BakedQuad(scaleVertex(quad.position0()), scaleVertex(quad.position1()), scaleVertex(quad.position2()), scaleVertex(quad.position3()), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.direction(), quad.materialInfo());
    }

    private static class InvisibleModel implements BlockStateModel{

        private final BlockStateModel model;

        public InvisibleModel(BlockStateModel model) {
            this.model = model;
        }

        @Override
        public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
            int start = parts.size();
            this.model.collectParts(random, parts);
            int end = parts.size();

            for(int i = start; i < end; i++) {
                BlockStateModelPart part = parts.get(i);
                if (!(part instanceof InvisibleBlockStateModelPart)) {
                    parts.set(i, new InvisibleBlockStateModelPart(part));
                }
            }

        }

        @Override
        public Material.Baked particleMaterial() {
            return this.model.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.model.materialFlags();
        }
    }

    private static final class InvisibleBlockStateModelPart implements BlockStateModelPart {
        private final BlockStateModelPart part;
        private final Map<Direction, List<BakedQuad>> directions = new HashMap<Direction, List<BakedQuad>>();

        private InvisibleBlockStateModelPart(BlockStateModelPart originalPart) {
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

        @Override
        public Material.Baked particleMaterial() {
            return this.part.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.part.materialFlags();
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
