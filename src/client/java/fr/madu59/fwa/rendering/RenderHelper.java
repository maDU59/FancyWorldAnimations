package fr.madu59.fwa.rendering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack.Pose;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;

public class RenderHelper {

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

    private static final Map<BlockState, BlockStateModel> INVISIBLE_MODEL_CACHE = new ConcurrentHashMap<>();
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
    }

    public static void renderModel(PoseStack poseStack, List<BlockStateModelPart> parts, float a, float r, float g, float b, int light){
        for (int i = 0; i < parts.size(); i++){
            for (int j = 0; j < DIRECTIONS_WITH_NULL.length; j++){
                renderQuads(poseStack, parts.get(i).getQuads(DIRECTIONS_WITH_NULL[j]), a, r, g, b, light);
            }
        }
    }

    public static void renderQuads(PoseStack poseStack, List<BakedQuad> bakedQuads, float a, float r, float g, float b, int light){
        for (int i = 0; i < bakedQuads.size(); i++){
            renderQuad(poseStack, bakedQuads.get(i), a, r, g, b, light, shouldShade);
        }
    }

    public static void renderQuad(PoseStack poseStack, BakedQuad bakedQuad, float a, float r, float g, float b, int light){
        renderQuad(poseStack, bakedQuad, a, r, g, b, light, shouldShade);
    }

    public static void renderQuad(PoseStack poseStack, BakedQuad bakedQuad, float a, float r, float g, float b, int light, boolean isShaded){
        Vector3fc dir = bakedQuad.direction().getUnitVec3f();
        float shade = isShaded? getShade(dir.x(), dir.y(), dir.z(), poseStack) : 1.0f;
        QuadInstance quadInstance = new QuadInstance();
        quadInstance.setLightCoords(light);
        quadInstance.setColor(ARGB.colorFromFloat(a,r*shade,g*shade,b*shade));
        RenderType renderType = a < 1.0f? RenderTypes.translucentMovingBlock() : bakedQuad.materialInfo().sprite().transparency().hasTranslucent()? RenderTypes.translucentMovingBlock() : RenderTypes.cutoutMovingBlock();
        putBakedQuad(collector, poseStack, bakedQuad, quadInstance, renderType);
    }

    public static void putBakedQuad(SubmitNodeCollector collector, PoseStack poseStack, BakedQuad bakedQuad, QuadInstance quadInstance, RenderType renderType){
        collector.submitCustomGeometry(
            poseStack,
            renderType,
            (Pose currentPose, VertexConsumer consumer) -> {
                consumer.putBakedQuad(
                    currentPose, bakedQuad, quadInstance
                );
            }
        );
    }

    public static float getShade(float nx, float ny, float nz, PoseStack poseStack){
        return getShade(nx, ny, nz, poseStack.last());
    }

    public static float getShade(float nx, float ny, float nz, PoseStack.Pose pose){
        float shade = 1f;
        if(shouldShade){
            Vector3f normal = new Vector3f(nx, ny, nz);
            normal.mul(pose.normal());
            normal.normalize(); // Might not be needed
            float nx2 = normal.x() * normal.x();
            float ny2 = normal.y() * normal.y();
            float nz2 = normal.z() * normal.z();

            float yShade = normal.y() > 0 ? topShade : bottomShade;
            shade =  (nx2 * XShade) + (ny2 * yShade) + (nz2 * ZShade);
        }
        return shade;
    }

    public static BlockStateModel getInvisibleModel(BlockStateModel originalModel, BlockState state){
        return (originalModel != null && originalModel instanceof InvisibleModel ? originalModel : INVISIBLE_MODEL_CACHE.computeIfAbsent(state, k -> new InvisibleModel(originalModel)));
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
