package fr.madu59.fwa.anims;

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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BellAnimation extends Animation{

    private ModelPart bellBody;
    private final BakedModel model;
    private final float hash;
    private BellBlockEntity bellBlockEntity;
    private final RandomSource random;
    private final Direction facing;
    private final BellAttachType attachment;
    private final ResourceLocation atlasId = ResourceLocation.tryParse("minecraft:textures/atlas/blocks.png");
    private final ResourceLocation textureId = ResourceLocation.tryParse("minecraft:entity/bell/bell_body");
    
    public BellAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        random = RandomSource.create(defaultState.getSeed(position));
        model = Minecraft.getInstance().getBlockRenderer().getBlockModel(defaultState);
        this.hash = position.hashCode();
        if(Minecraft.getInstance().level.getBlockEntity(position) instanceof BellBlockEntity bbe){
            this.bellBlockEntity = bbe;
        }
        else if (Minecraft.getInstance().level.getBlockEntity(position) != null){
            FancyWorldAnimationsClient.removeAnimationAt(position);
        }

        facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        attachment = defaultState.getValue(BlockStateProperties.BELL_ATTACHMENT);
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

        return ((slowWave + fastWave) / 2f) * Math.min(Math.max(time - (float)getAnimDuration(), 0f), 5f)/5f;
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
        if (this.bellBody == null) {
            return;
        }
        if(bellBlockEntity == null){
            if(Minecraft.getInstance().level.getBlockEntity(position) instanceof BellBlockEntity bbe){
                bellBlockEntity = bbe;
            }
            else if (Minecraft.getInstance().level.getBlockEntity(position) != null){
                FancyWorldAnimationsClient.removeAnimationAt(position);
                System.out.println("[REMOVAL] Invalid block removed");
            }
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(atlasId).apply(textureId);
        PoseStack poseStack = context.getPoseStack();

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        setupAnim(bellBlockEntity, Math.min(Math.max(Minecraft.getInstance().getFrameTime(), 0f), 1.0f));

        if (bellBlockEntity.ticks == 0) {
            float time = (float)(context.getNowTick() - this.startTick);
            float rot;
            if (time <= getAnimDuration()) rot = animatePlacement(context.getNowTick());
            else rot = animateIdle(time);
            bellBody = rotateBell(bellBody, rot, facing, attachment);
        }

        bellBody.render(poseStack, sprite.wrap(RenderHelper.getBuffer()), light, OverlayTexture.NO_OVERLAY);

        if(shouldUseFallbackRender()){
            VertexConsumer buffer = RenderHelper.getBuffer();
            RenderHelper.renderModel(buffer, poseStack.last(), model, 1.0f, 1.0f, 1.0f, 1.0f, light, random, defaultState);
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
}
