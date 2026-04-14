package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ChainAnimation extends Animation{

    private float tiltX = 0f;
    private float tiltZ = 0f;
    private float spin = 0f;
    private int chainCount = 0;
    private List<BlockStateModelPart> parts = new ArrayList<>();
    private BlockStateModel model;
    private final Quaternionf combined = new Quaternionf();
    
    public ChainAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
    }

    @Override
    public boolean isFinished(double nowTick) {
        return false;
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.LANTERN_STATE.getValue() || SettingsManager.CHAIN_STATE.getValue();
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.LANTERN_STATE.getValue() || SettingsManager.CHAIN_STATE.getValue();
    }

    @Override
    public boolean isRendering(){
        return Boolean.TRUE.equals(isLast);
    }

    @Override
    public void setLast(boolean isLast){
        if(this.isLast == null || this.isLast != isLast){
            super.setLast(isLast);
            if (isLast) needUpdate();
        }
    }

    @Override
    public AABB getBoundingBox(){
        return new AABB(position.getCenter().add(-0.5, -0.5, -0.5), position.above(chainCount).getCenter().add(0.5, 0.5, 0.5));
    }

    public void update(){
        ClientLevel level = Minecraft.getInstance().level;
        if(isLast == null && !level.getBlockState(position).isAir()){
            isLast = SwingingBlockHelper.isLast(position);
            if (SettingsManager.CHAIN_GROUNDED.getValue() && isLast && !level.getBlockState(position.below()).isAir()) FancyWorldAnimationsClient.onBlockUpdate(position, defaultState, defaultState);
        }
        chainCount = SwingingBlockHelper.getChainCount(position);
        needUpdate = false;
    }

    @Override
    public void tick(double nowTick) {
        if(isLast == null){
            update();
            return;
        }
        if (!isLast) return;
        if (needUpdate) update();
    }

    @Override
    public void render(AnimationRenderingContext context) {
        if (!isLast) return;
        ClientLevel level = Minecraft.getInstance().level;
        float swingScale = 0.7F;
        if(SettingsManager.CHAIN_SWING_LIMIT.getValue()) swingScale = 0.7F/(float)Math.sqrt(Math.max(4,chainCount)-3);
        float prevFactor = 0.0F;
        VertexConsumer buffer = RenderHelper.getBuffer();
        PoseStack poseStack = context.getPoseStack();
        extractRenderState(context);
        float degToRad = (float) Math.PI / 180.0f;
        float tiltX = this.tiltX * swingScale * degToRad;
        float tiltZ = this.tiltZ * swingScale * degToRad;
        float spin = this.spin * Math.max(0.55F, swingScale) * degToRad;
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
                combined.identity()
                    .rotateZ(tiltZ * deltaFactor)
                    .rotateX(tiltX * deltaFactor)
                    .rotateY(spin * deltaFactor);
                poseStack.mulPose(combined);
            }
            poseStack.pushPose();
            poseStack.translate(-0.5F, -1.0F, -0.5F);
            parts.clear();
            BlockState chainState = level.getBlockState(mutable);
            int light = LevelRenderer.getLightCoords(LevelRenderer.BrightnessGetter.DEFAULT ,(BlockAndLightGetter) level, chainState, mutable);
            RandomSource random = RandomSource.create(chainState.getSeed(mutable));
            model = Minecraft.getInstance().getBlockRenderer().getBlockModel(chainState);
            model.collectParts(random, parts);
            RenderHelper.renderModel(buffer, poseStack.last(), parts, 1.0f, 1.0f, 1.0f, 1.0f, light);
            poseStack.popPose();
            poseStack.translate(0.0F, -1.0F, 0.0F);
            mutable.move(0,-1,0);
        }
        poseStack.popPose();
    }

    public void extractRenderState(AnimationRenderingContext context) {
        float posOffset = (position.getX() * 0.6f) + (position.getZ() * 0.6f);
        float uniqueTime = ((float)context.getNowTick()) * 0.1f + posOffset;

        this.tiltX = (float) Math.sin(uniqueTime) * 8f;
        this.tiltZ = (float) Math.cos(uniqueTime * 0.8f) * 6f;
        this.spin = (float) Math.sin(uniqueTime * 1.5f) * 4f;
    }
}
