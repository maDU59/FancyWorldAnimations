package fr.madu59.fwa.rendering;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.math.Axis;

import fr.madu59.fwa.blockentity.LanternBlockEntity;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.LanternBlockInterface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LanternRenderer implements BlockEntityRenderer<LanternBlockEntity, LanternRenderState>{
    private final BlockRenderDispatcher blockRenderDispatcher;

    public LanternRenderer(BlockEntityRendererProvider.Context context){
        blockRenderDispatcher = context.blockRenderDispatcher();
    }

    public LanternRenderState createRenderState(){
        return new LanternRenderState();
    }

    public void submit(LanternRenderState lanternRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState){
        if(lanternRenderState.movingState == null || !SettingsManager.LANTERN_STATE.getValue()) return;
        float pivotY = lanternRenderState.hanging ? 1.0F : 0.5F;
        float swingScale = 1;
        float yaw = lanternRenderState.yaw * Math.max(0.55F, swingScale);
        float tiltX = lanternRenderState.tiltX * swingScale;
        float tiltZ = lanternRenderState.tiltZ * swingScale;
        float spin = lanternRenderState.spin * Math.max(0.55F, swingScale);
        poseStack.pushPose();
        poseStack.translate(0.5F, pivotY, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(tiltZ));
        poseStack.mulPose(Axis.XP.rotationDegrees(tiltX));
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.translate(-0.5F, -pivotY, -0.5F);
        if (lanternRenderState.hanging) {
            poseStack.translate(0.0F, 0.03F, 0.0F);
        }
        submitNodeCollector.submitMovingBlock(poseStack, lanternRenderState.movingState);
        this.renderCrumblingOverlay(submitNodeCollector, poseStack, lanternRenderState);
        poseStack.popPose();
    }

    public void extractRenderState(LanternBlockEntity lanternBlockEntity, LanternRenderState lanternRenderState, float tickDelta, Vec3 offset, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if(!lanternBlockEntity.isEnabled()) return;
        BlockEntityRenderer.super.extractRenderState(lanternBlockEntity, lanternRenderState, tickDelta, offset, crumblingOverlay);
        Level level = lanternBlockEntity.getLevel();
        BlockPos blockPos = lanternBlockEntity.getBlockPos();

        BlockPos pos = lanternBlockEntity.getBlockPos();
        float posOffset = (pos.getX() * 0.6f) + (pos.getZ() * 0.6f);
        float uniqueTime = (level.getGameTime() + tickDelta) * 0.1f + posOffset;

        lanternRenderState.tiltX = (float) Math.sin(uniqueTime) * 12f;
        lanternRenderState.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 8f;

        lanternRenderState.spin = (float) Math.sin(uniqueTime * 1.5f) * 5f;

        if(level != null && lanternBlockEntity.isHanging()){
            lanternRenderState.hanging = true;
            int lastCrumbleStage = lanternRenderState.crumbleStage;
            int crumbleStage = -1;
            int maxCrumbleStage = ModelBakery.DESTROY_STAGE_COUNT;
            if (crumblingOverlay != null){
                crumbleStage = Mth.clamp(crumblingOverlay.progress(), 0, maxCrumbleStage - 1);
                if (level instanceof ClientLevel clientLevel) {
                    long time = clientLevel.getGameTime();
                    if (crumbleStage != lastCrumbleStage || time - lanternRenderState.lastCrumbleParticleTime >= 10L) {
                        addBreakingBlockEffect(clientLevel, blockPos, Direction.getRandom(clientLevel.getRandom()));
                        lanternRenderState.lastCrumbleParticleTime = time;
                    }
               }
            }
            lanternRenderState.crumbleStage = crumbleStage;
            lanternRenderState.movingState = getMovingBlockRenderState(lanternRenderState.movingState, lanternBlockEntity.getBlockState(), blockPos, crumblingOverlay, crumbleStage, level);
        }
        else{
            lanternRenderState.hanging = false;
        }
    }

    public void renderCrumblingOverlay(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LanternRenderState lanternRenderState){
        if(lanternRenderState.crumbleStage < 0) return;
        if(lanternRenderState.movingState != null){
            RenderType renderType = (RenderType) ModelBakery.DESTROY_TYPES.get(lanternRenderState.crumbleStage);
            BlockPos blockPos = lanternRenderState.movingState.blockPos;
            BlockState blockState = lanternRenderState.movingState.blockState;
            submitNodeCollector.submitCustomGeometry(poseStack, renderType, (matrixEntry, vertexConsumer) -> {
                PoseStack stack = new PoseStack();
                stack.last().pose().set(matrixEntry.pose());
                stack.last().normal().set(matrixEntry.normal());
                BlockStateModel model = this.blockRenderDispatcher.getBlockModel(blockState);
                RandomSource random = RandomSource.create(blockState.getSeed(blockPos));
                List<BlockModelPart> parts = model.collectParts(random);
                if (!parts.isEmpty()) {
                    this.blockRenderDispatcher.getModelRenderer().tesselateBlock(lanternRenderState.movingState.level, parts, blockState, blockPos, stack, new SheetedDecalTextureGenerator(vertexConsumer, stack.last(), 1.0F), true, OverlayTexture.NO_OVERLAY);
                }
            });
        }

    }

    public MovingBlockRenderState getMovingBlockRenderState(MovingBlockRenderState movingState, BlockState blockState, BlockPos blockPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int crumbleStage, Level level){
        ClientLevel clientLevel;
        if(level instanceof ClientLevel cl){
            clientLevel = cl;
        }
        else{
            clientLevel = null;
        }
        
        if(crumbleStage >= 0 && clientLevel != null && crumblingOverlay != null){
            LanternBreakingRenderState renderState;
            if(movingState instanceof LanternBreakingRenderState lanternBreakingRenderState){
                renderState = lanternBreakingRenderState;
            }
            else{
                renderState = new LanternBreakingRenderState(clientLevel, blockPos, crumbleStage);
            }
            renderState.level = level;
            renderState.blockPos = blockPos;
            renderState.randomSeedPos = blockPos;
            renderState.progress = crumbleStage;
            renderState.blockState = blockState;
            renderState.biome = level.getBiome(blockPos);
            return renderState;
        }
        else{
            MovingBlockRenderState renderState = movingState;
            if(movingState == null || movingState instanceof LanternBreakingRenderState){
                renderState = new MovingBlockRenderState();
            }
            renderState.level = level;
            renderState.blockPos = blockPos;
            renderState.randomSeedPos = blockPos;
            renderState.blockState = blockState;
            renderState.biome = level.getBiome(blockPos);
            return renderState;
        }
    }

    public void addBreakingBlockEffect(ClientLevel clientLevel, BlockPos blockPos, Direction direction) {
        BlockState blockState = clientLevel.getBlockState(blockPos);
        if (blockState.shouldSpawnTerrainParticles()) {
            int i = blockPos.getX();
            int j = blockPos.getY();
            int k = blockPos.getZ();
            AABB aABB = ((LanternBlockInterface)blockState.getBlock()).fwa$getShape().bounds();
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

            Minecraft.getInstance().particleEngine.add((new TerrainParticle(clientLevel, d, e, g, (double)0.0F, (double)0.0F, (double)0.0F, blockState, blockPos)).setPower(0.2F).scale(0.6F));
        }
   }
}
