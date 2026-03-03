package fr.madu59.fwa.anims;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.rendering.AnimationRenderingContext;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;

public class BellAnimation extends Animation{

    private ModelPart bellBody;
    private final RandomSource random = RandomSource.create(42);
    private final BakedModel model;
    private final float hash;
    private BellBlockEntity bellBlockEntity = (BellBlockEntity) Minecraft.getInstance().level.getBlockEntity(position);
    
    public BellAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        this.hash = position.hashCode();
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
        ModelPart modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BELL);
        this.bellBody = modelPart.getChild("bell_body");
        if (bellBlockEntity == null || this.bellBody == null) {
            bellBlockEntity = (BellBlockEntity) Minecraft.getInstance().level.getBlockEntity(position);
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(ResourceLocation.tryParse("minecraft:entity/bell/bell_body"));        PoseStack poseStack = context.getPoseStack();
        Direction facing = defaultState.getValue(BellBlock.FACING);
        BellAttachType attachment = defaultState.getValue(BellBlock.ATTACHMENT);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        setupAnim(bellBlockEntity, Math.clamp(Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true), 0.0f, 1.0f));

        if (bellBlockEntity.ticks == 0) {
            float time = (float)(context.getNowTick() - this.startTick);
            float rot;
            if (time <= getAnimDuration()) rot = animatePlacement(context.getNowTick());
            else rot = animateIdle(time);
            bellBody = rotateBell(bellBody, rot, facing, attachment);
        }

        bellBody.render(poseStack, sprite.wrap(context.getBufferSource().getBuffer(RenderType.cutoutMipped())), light, OverlayTexture.NO_OVERLAY, -1);

        if(shouldUseFallbackRender()){
            VertexConsumer buffer = context.getBufferSource().getBuffer(RenderType.cutoutMipped());
            renderQuads(poseStack, buffer, model.getQuads(defaultState, null, random), light);
            for(Direction dir : Direction.values()){
                renderQuads(poseStack, buffer, model.getQuads(defaultState, dir, random), light);
            }
        }
    }

    public void setupAnim(BellBlockEntity bellBlockEntity, float f) {
        float g = (float)bellBlockEntity.ticks + f;
        float h = 0.0F;
        float k = 0.0F;
        if (bellBlockEntity.shaking) {
            float l = Mth.sin(g / 3.1415927F) / (4.0F + g / 3.0F);
            if (bellBlockEntity.clickDirection == Direction.NORTH) {
                h = -l;
            } else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
                h = l;
            } else if (bellBlockEntity.clickDirection == Direction.EAST) {
                k = -l;
            } else if (bellBlockEntity.clickDirection == Direction.WEST) {
                k = l;
            }
        }

        this.bellBody.xRot = h;
        this.bellBody.zRot = k;
    }

    private void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, int light) {
        for (BakedQuad quad : quads) {
            buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, light, OverlayTexture.NO_OVERLAY);
        }
    }
}
