package fr.madu59.fwa.anims;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.rendering.RenderHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;

public class BellAnimation extends Animation{

    private final Minecraft client = Minecraft.getInstance();
    private final BellModel bellModel;
    private final BlockStateModel model;
    private final float hash;
    private BellBlockEntity bellBlockEntity;
    private List<BlockModelPart> parts = new ArrayList<>();
    
    public BellAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        this.bellModel = new BellModel(client.getEntityModels().bakeLayer(ModelLayers.BELL));
        RandomSource random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        model.collectParts(random, parts);
        this.hash = position.hashCode();
        if(client.level.getBlockEntity(position) instanceof BellBlockEntity bbe){
            this.bellBlockEntity = bbe;
        }
        else if (client.level.getBlockEntity(position) != null){
            FancyWorldAnimationsClient.removeAnimationAt(position);
        }
    }

    @Override
    public double getAnimDuration() {
        return 50 / SettingsManager.BELL_SPEED.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.BELL_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return shouldUseFallbackRender();
    }

    private boolean shouldUseFallbackRender() {
        return FabricLoader.getInstance().isModLoaded("betterblockentities");
    }

    public static boolean hasInfiniteAnimation(){
        return SettingsManager.BELL_INFINITE.getValue();
    }

    @Override
    public double getLifeSpan(){
        return !hasInfiniteAnimation()? getAnimDuration() : Double.MAX_VALUE;
    }

    private float animateIdle(float time){
        float uniqueOffset = (float)(hash % 100);
        time += uniqueOffset;
        float slowWave = (float) Math.sin(time * 0.1f) * 0.05f;

        float fastWave = (float) Math.sin(time * 0.25f) * 0.02f;

        return ((slowWave + fastWave) / 2f) * Math.clamp(time - (float)getAnimDuration(), 0f, 5f)/5f;
    }

    private float animatePlacement(double nowTick){
        double progress = getProgress(nowTick);
        return (float)Math.sin(progress * getAnimDuration() / 3.1415927) / (float)(4.0 + progress * getAnimDuration() / 3.0);
    }

    private ModelPart rotateBell(ModelPart bellBody, float rot, Direction facing, BellAttachType attachment){
        float f = 0.0F;
        float g = 0.0F;
        switch (facing) {
            case NORTH:
                f = -rot;
                break;
            case SOUTH:
                f = rot;
                break;
            case WEST:
                g = -rot;
                break;
            case EAST:
                g = rot;
                break;
            default:
                break;
        }

        if(attachment == BellAttachType.SINGLE_WALL){
            bellBody.xRot = g;
            bellBody.zRot = f;
        }
        else{
            bellBody.xRot = f;
            bellBody.zRot = g;
            }
        return bellBody;
    }

    @Override
    public void render(AnimationRenderingContext context) {
        if(bellBlockEntity == null){
            if(client.level.getBlockEntity(position) instanceof BellBlockEntity bbe){
                bellBlockEntity = bbe;
            }
            else if (client.level.getBlockEntity(position) != null){
                FancyWorldAnimationsClient.removeAnimationAt(position);
                System.out.println("[REMOVAL] Invalid block removed");
            }
            return;
        }
        PoseStack poseStack = context.getPoseStack();
        TextureAtlasSprite sprite = client.getAtlasManager().getAtlasOrThrow(ResourceLocation.tryParse("minecraft:blocks")).getSprite(ResourceLocation.tryParse("minecraft:entity/bell/bell_body"));
        Direction facing = defaultState.getValue(BellBlock.FACING);
        BellAttachType attachment = defaultState.getValue(BellBlock.ATTACHMENT);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) client.level, position);

        float ticks = bellBlockEntity.ticks + Math.clamp(client.getDeltaTracker().getGameTimeDeltaPartialTick(true), 0.0f, 1.0f);
        Direction shakeDirection = bellBlockEntity.shaking ? bellBlockEntity.clickDirection : null;
        bellModel.setupAnim(new BellModel.State(ticks, shakeDirection));

        ModelPart bellBody = bellModel.getChildPart("bell_body");

        if (bellBlockEntity.ticks == 0) {
            float time = (float)(context.getNowTick() - this.startTick);
            float rot;
            if (time <= getAnimDuration()) rot = animatePlacement(context.getNowTick());
            else rot = animateIdle(time);
            bellBody = rotateBell(bellBody, rot, facing, attachment);
        }

        context.getSubmitNodeCollector().submitModelPart(bellBody, poseStack, RenderType.cutoutMipped(), light, OverlayTexture.NO_OVERLAY, sprite);

        if(shouldUseFallbackRender()){
            VertexConsumer buffer = context.getBufferSource().getBuffer(RenderType.cutoutMipped());
            RenderHelper.renderModel(buffer, poseStack.last(), parts, 1.0f, 1.0f, 1.0f, 1.0f, light);
        }
    }
}
