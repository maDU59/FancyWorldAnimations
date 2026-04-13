package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.compat.ModCompat;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import fr.madu59.fwa.utils.Curves;
import fr.madu59.fwa.utils.SwingingBlockHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class LanternAnimation extends Animation{

    private float tiltX = 0f;
    private float tiltZ = 0f;
    private float spin = 0f;
    private List<BlockModelPart> parts = new ArrayList<>();
    private final BlockStateModel model;
    private int chainCount;
    private List<BlockModelPart> chainParts = new ArrayList<>();
    private final Quaternionf combined = new Quaternionf();
    
    public LanternAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        model.collectParts(random, parts);
        chainCount = SwingingBlockHelper.getChainCount(position);
    }

    @Override
    public boolean isFinished(double nowTick) {
        return false;
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlockEntity() {
        return !ModCompat.WW_DISPLAY_LANTERNS.equals(BuiltInRegistries.BLOCK.getKey(defaultState.getBlock()));
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.LANTERN_STATE.getValue();
    }

    @Override
    public AABB getBoundingBox(){
        return new AABB(position.getCenter().add(-0.5, -0.5, -0.5), position.above(chainCount).getCenter().add(0.5, 0.5, 0.5));
    }

    @Override
    public void setLast(boolean isLast){
        if(this.isLast == null || this.isLast != isLast){
            super.setLast(isLast);
            if (isLast) needUpdate();
        }
    }

    public void update(){
        if(!SettingsManager.CHAIN_STATE.getValue() && !SettingsManager.LANTERN_OVERRIDE.getValue()) chainCount = 1;
        else chainCount = SwingingBlockHelper.getChainCount(position);
        needUpdate = false;
    }

    @Override
    public void render(AnimationRenderingContext context) {
        if (needUpdate) update();
        VertexConsumer buffer = RenderHelper.getBuffer();
        PoseStack poseStack = context.getPoseStack();
        ClientLevel level = Minecraft.getInstance().level;
        extractRenderState(context);
        float swingScale = 0.7f;
        if(SettingsManager.CHAIN_SWING_LIMIT.getValue()) swingScale = 0.7F/(float)Math.sqrt(Math.max(4,chainCount)-3);
        float degToRad = (float) Math.PI / 180.0f;
        float tiltX = this.tiltX * swingScale * degToRad;
        float tiltZ = this.tiltZ * swingScale * degToRad;
        float spin = this.spin * Math.max(0.55F, swingScale) * degToRad;
        poseStack.pushPose();
        poseStack.translate(0.5F, chainCount, 0.5F);
        float prevFactor = 0.0F;
        MutableBlockPos mutable = position.mutable().move(0,chainCount-1,0);
        for (int index = chainCount - 1; index >= 1; --index) {
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
            BlockState chainState = level.getBlockState(mutable);
            int light = LevelRenderer.getLightColor(LevelRenderer.BrightnessGetter.DEFAULT, (BlockAndTintGetter) level, chainState, mutable);
            chainParts.clear();
            BlockStateModel chainModel;
            RandomSource random = RandomSource.create(chainState.getSeed(mutable));
            chainModel =Minecraft.getInstance().getBlockRenderer().getBlockModel(chainState);
            chainModel.collectParts(random, chainParts);
            RenderHelper.renderModel(buffer, poseStack.last(), chainParts, 1.0f, 1.0f, 1.0f, 1.0f, light);
            poseStack.popPose();
            poseStack.translate(0.0F, -1.0F, 0.0F);
            mutable.move(0,-1,0);
        }
        float deltaFactor = 1f - prevFactor;
        if (deltaFactor != 0.0F) {
            combined.identity()
                    .rotateZ(tiltZ * deltaFactor)
                    .rotateX(tiltX * deltaFactor)
                    .rotateY(spin * deltaFactor);
                poseStack.mulPose(combined);
        }
        poseStack.pushPose();
        poseStack.translate(-0.5F, -1.0F, -0.5F);
        poseStack.translate(0.0F, 0.03F, 0.0F);
        int light = LevelRenderer.getLightColor((BlockAndTintGetter) level, position);
        RenderHelper.renderModel(buffer, poseStack.last(), parts, 1.0f, 1.0f, 1.0f, 1.0f, light);
        poseStack.popPose();
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
