package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.mixin.client.LevelRendererAccessor;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ChainAnimation extends Animation{

    private float tiltX = 0f;
    private float tiltZ = 0f;
    private float yaw = 0f;
    private float spin = 0f;
    private int crumbleStage = -1;
    private long lastCrumbleParticleTime = 0L;
    private int lastTick = 0;
    private BlockState state;
    private List<BlockModelPart> parts = new ArrayList<>();
    private BlockStateModel model;
    private PoseStack stack = new PoseStack();
    
    public ChainAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newState, BlockState oldState) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        state = newState;
    }

    @Override
    public double getLifeSpan(){
        return SettingsManager.LANTERN_STATE.getValue()? Double.MAX_VALUE : 0;
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public void render(AnimationRenderingContext context) {
        if (!SwingingBlockHelper.isLast(position)) return;
        ClientLevel level = Minecraft.getInstance().level;
        float swingScale = 0.7f;
        float prevFactor = 0.0F;
        if ((SettingsManager.CHAIN_GROUNDED.getValue() && !level.getBlockState(position.below()).isAir()) || !SettingsManager.CHAIN_STATE.getValue()) {
            swingScale = 0;
            prevFactor = 1.0F;
        }
        VertexConsumer buffer = context.getBufferSource().getBuffer(RenderType.cutoutMipped());
        int chainCount = SwingingBlockHelper.getChainCount(position);
        PoseStack poseStack = context.getPoseStack();
        extractRenderState(context);
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        float yaw = this.yaw * Math.max(0.55F, swingScale);
        float tiltX = this.tiltX * swingScale;
        float tiltZ = this.tiltZ * swingScale;
        float spin = this.spin * Math.max(0.55F, swingScale);
        poseStack.pushPose();
        poseStack.translate(0.5F, chainCount, 0.5F);
        MutableBlockPos mutable = position.mutable().move(0,chainCount-1,0);
        for (int index = chainCount - 1; index >= 0; --index) {
            float t = (float)(chainCount - index) / (float)chainCount;
            t = Mth.clamp(t, 0.0F, 1.0F);
            float targetFactor = Curves.smooth(t);
            float deltaFactor = targetFactor - prevFactor;
            prevFactor = targetFactor;
            
            if (deltaFactor != 0.0F) {
                poseStack.mulPose(Axis.YP.rotationDegrees(yaw * deltaFactor));
                poseStack.mulPose(Axis.ZP.rotationDegrees(tiltZ * deltaFactor));
                poseStack.mulPose(Axis.XP.rotationDegrees(tiltX * deltaFactor));
                poseStack.mulPose(Axis.YP.rotationDegrees(spin * deltaFactor));
            }
            poseStack.pushPose();
            poseStack.translate(-0.5F, -1.0F, -0.5F);
            parts = new ArrayList<>();
            BlockState chainState = level.getBlockState(mutable);
            RandomSource random = RandomSource.create(chainState.getSeed(mutable));
            model = Minecraft.getInstance().getBlockRenderer().getBlockModel(chainState);
            model.collectParts(random, parts);
            RenderHelper.renderModel(buffer, poseStack.last(), parts, 1.0f, 1.0f, 1.0f, 1.0f, light);
            poseStack.popPose();
            poseStack.translate(0.0F, -1.0F, 0.0F);
            mutable.move(0,-1,0);
        }
        //this.renderCrumblingOverlay(context.getSubmitNodeCollector(), poseStack);
        poseStack.popPose();
    }

    public void extractRenderState(AnimationRenderingContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        float posOffset = (position.getX() * 0.6f) + (position.getZ() * 0.6f);
        float uniqueTime = ((float)context.getNowTick()) * 0.1f + posOffset;

        this.tiltX = (float) Math.sin(uniqueTime) * 8f;
        this.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 6f;

        this.spin = (float) Math.sin(uniqueTime * 1.5f) * 4f;

        if(level != null){
            int lastCrumbleStage = this.crumbleStage;
            int crumbleStage = getCrumblingStage(context.getNowTick());
            int maxCrumbleStage = ModelBakery.DESTROY_STAGE_COUNT;
            if (crumbleStage >= 0){
                crumbleStage = Mth.clamp(crumbleStage, 0, maxCrumbleStage - 1);
                long time = level.getGameTime();
                if (crumbleStage != lastCrumbleStage || time - this.lastCrumbleParticleTime >= 10L) {
                    addBreakingBlockEffect(level, Direction.getRandom(level.getRandom()));
                    this.lastCrumbleParticleTime = time;
                }
            }
            this.crumbleStage = crumbleStage;
        }
    }

    public int getCrumblingStage(double nowTick){
        if(lastTick - Mth.floor(nowTick) >= 0.5) return crumbleStage;
        lastTick = Mth.floor(nowTick);
        Long2ObjectMap<SortedSet<BlockDestructionProgress>> progressMap = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).fwa$getDestructionProgress();
        SortedSet<BlockDestructionProgress> sortedSet = progressMap.get(position.asLong());
        if (sortedSet != null && !sortedSet.isEmpty()) {
            return sortedSet.last().getProgress();
        }
        return -1;
    }

    // public void renderCrumblingOverlay(SubmitNodeCollector submitNodeCollector, PoseStack poseStack){
    //     if(this.crumbleStage < 0) return;
    //     ClientLevel level = Minecraft.getInstance().level;
    //     RenderType renderType = (RenderType) ModelBakery.DESTROY_TYPES.get(this.crumbleStage);
    //     submitNodeCollector.submitCustomGeometry(poseStack, renderType, (matrixEntry, vertexConsumer) -> {
    //         stack.last().pose().set(matrixEntry.pose());
    //         stack.last().normal().set(matrixEntry.normal());
    //         if (!parts.isEmpty()) {
    //             Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, parts, state, position, stack, new SheetedDecalTextureGenerator(vertexConsumer, stack.last(), 1.0F), true, OverlayTexture.NO_OVERLAY);
    //         }
    //     });
    // }

    public void addBreakingBlockEffect(ClientLevel clientLevel, Direction direction) {
        if (state.shouldSpawnTerrainParticles()) {
            int i = position.getX();
            int j = position.getY();
            int k = position.getZ();
            AABB aABB = state.getShape(clientLevel, position).bounds();
            double d = (double)i + clientLevel.getRandom().nextDouble() * (aABB.maxX - aABB.minX - (double)0.2F) + (double)0.1F + aABB.minX;
            double e = (double)j + clientLevel.getRandom().nextDouble() * (aABB.maxY - aABB.minY - (double)0.2F) + (double)0.1F + aABB.minY;
            double g = (double)k + clientLevel.getRandom().nextDouble() * (aABB.maxZ - aABB.minZ - (double)0.2F) + (double)0.1F + aABB.minZ;
            if (direction == Direction.DOWN) {
                e = (double)j + aABB.minY - (double)0.1F;
            }

            if (direction == Direction.UP) {
                e = (double)j + aABB.maxY + (double)0.1F;
            }

            if (direction == Direction.NORTH) {
                g = (double)k + aABB.minZ - (double)0.1F;
            }

            if (direction == Direction.SOUTH) {
                g = (double)k + aABB.maxZ + (double)0.1F;
            }

            if (direction == Direction.WEST) {
                d = (double)i + aABB.minX - (double)0.1F;
            }

            if (direction == Direction.EAST) {
                d = (double)i + aABB.maxX + (double)0.1F;
            }

            Minecraft.getInstance().particleEngine.add((new TerrainParticle(clientLevel, d, e, g, (double)0.0F, (double)0.0F, (double)0.0F, state, position)).setPower(0.2F).scale(0.6F));
        }
    }
}
