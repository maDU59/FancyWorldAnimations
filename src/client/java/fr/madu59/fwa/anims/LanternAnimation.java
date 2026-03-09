package fr.madu59.fwa.anims;

import java.util.List;
import java.util.SortedSet;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.math.Axis;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.mixin.client.LevelRendererAccessor;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.LanternBlockInterface;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LanternAnimation extends Animation{

    private float tiltX = 0f;
    private float tiltZ = 0f;
    private float yaw = 0f;
    private float spin = 0f;
    private int crumbleStage = -1;
    private long lastCrumbleParticleTime = 0L;
    private BlockState state;
    
    public LanternAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState newState, BlockState oldState) {
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
        PoseStack poseStack = context.getPoseStack();
        SubmitNodeCollector submitNodeCollector = context.getSubmitNodeCollector();
        extractRenderState(context);
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);
        float swingScale = 1;
        float yaw = this.yaw * Math.max(0.55F, swingScale);
        float tiltX = this.tiltX * swingScale;
        float tiltZ = this.tiltZ * swingScale;
        float spin = this.spin * Math.max(0.55F, swingScale);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.0F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        poseStack.mulPose(Axis.XP.rotationDegrees(tiltX));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.translate(-0.5F, -1.0F, -0.5F);
        poseStack.translate(0.0F, 0.03F, 0.0F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(defaultState, poseStack, context.getBufferSource(), light, OverlayTexture.NO_OVERLAY);
        this.renderCrumblingOverlay(submitNodeCollector, poseStack);
        poseStack.popPose();
    }

    public void extractRenderState(AnimationRenderingContext context) {
        ClientLevel level = Minecraft.getInstance().level;
        BlockPos pos = position;
        float posOffset = (pos.getX() * 0.6f) + (pos.getZ() * 0.6f);
        float uniqueTime = ((float)context.getNowTick()) * 0.1f + posOffset;

        this.tiltX = (float) Math.sin(uniqueTime) * 8f;
        this.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 6f;

        this.spin = (float) Math.sin(uniqueTime * 1.5f) * 4f;

        if(level != null){
            int lastCrumbleStage = this.crumbleStage;
            int crumbleStage = getCrumblingStage(pos);
            int maxCrumbleStage = ModelBakery.DESTROY_STAGE_COUNT;
            if (crumbleStage >= 0){
                crumbleStage = Mth.clamp(crumbleStage, 0, maxCrumbleStage - 1);
                long time = level.getGameTime();
                if (crumbleStage != lastCrumbleStage || time - this.lastCrumbleParticleTime >= 10L) {
                    addBreakingBlockEffect(level, pos, Direction.getRandom(level.getRandom()));
                    this.lastCrumbleParticleTime = time;
                }
            }
            this.crumbleStage = crumbleStage;
        }
    }

    public int getCrumblingStage(BlockPos pos){
        Long2ObjectMap<SortedSet<BlockDestructionProgress>> progressMap = ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).fwa$getDestructionProgress();
        SortedSet<BlockDestructionProgress> sortedSet = progressMap.get(pos.asLong());
        if (sortedSet != null && !sortedSet.isEmpty()) {
            return sortedSet.last().getProgress();
        }
        return -1;
    }

    public void renderCrumblingOverlay(SubmitNodeCollector submitNodeCollector, PoseStack poseStack){
        if(this.crumbleStage < 0) return;
        ClientLevel level = Minecraft.getInstance().level;
        RenderType renderType = (RenderType) ModelBakery.DESTROY_TYPES.get(this.crumbleStage);
        BlockPos blockPos = position;
        BlockState blockState = state;
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (matrixEntry, vertexConsumer) -> {
            PoseStack stack = new PoseStack();
            stack.last().pose().set(matrixEntry.pose());
            stack.last().normal().set(matrixEntry.normal());
            BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState);
            RandomSource random = RandomSource.create(blockState.getSeed(blockPos));
            List<BlockModelPart> parts = model.collectParts(random);
            if (!parts.isEmpty()) {
                Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(level, parts, blockState, blockPos, stack, new SheetedDecalTextureGenerator(vertexConsumer, stack.last(), 1.0F), true, OverlayTexture.NO_OVERLAY);
            }
        });
    }

    public void addBreakingBlockEffect(ClientLevel clientLevel, BlockPos blockPos, Direction direction) {
        if (state.shouldSpawnTerrainParticles()) {
            int i = blockPos.getX();
            int j = blockPos.getY();
            int k = blockPos.getZ();
            AABB aABB = ((LanternBlockInterface)state.getBlock()).fwa$getShape().bounds();
            double d = (double)i + clientLevel.random.nextDouble() * (aABB.maxX - aABB.minX - (double)0.2F) + (double)0.1F + aABB.minX;
            double e = (double)j + clientLevel.random.nextDouble() * (aABB.maxY - aABB.minY - (double)0.2F) + (double)0.1F + aABB.minY;
            double g = (double)k + clientLevel.random.nextDouble() * (aABB.maxZ - aABB.minZ - (double)0.2F) + (double)0.1F + aABB.minZ;
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

            Minecraft.getInstance().particleEngine.add((new TerrainParticle(clientLevel, d, e, g, (double)0.0F, (double)0.0F, (double)0.0F, state, blockPos)).setPower(0.2F).scale(0.6F));
        }
   }
}
