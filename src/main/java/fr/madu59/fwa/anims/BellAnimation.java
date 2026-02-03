package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.FwaModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;

public class BellAnimation extends Animation{

    private final BellModel bellModel;
    private final float hash;
    private BellBlockEntity bellBlockEntity = (BellBlockEntity) Minecraft.getInstance().level.getBlockEntity(position);
    
    public BellAnimation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        super(position, defaultState, startTick, oldIsOpen, newIsOpen);
        this.bellModel = new BellModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.BELL));
        this.hash = position.hashCode();
    }

    @Override
    public double getAnimDuration() {
        return 50 * SettingsManager.BELL_SPEED.getValue();
    }

    @Override
    public boolean isEnabled(){
        return SettingsManager.BELL_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
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
        return Mth.sin(progress * getAnimDuration() / 3.1415927) / (float)(4.0 + progress * getAnimDuration() / 3.0);
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
    public void render(PoseStack poseStack, BufferSource bufferSource, double nowTick) {
        if(bellBlockEntity == null){
            bellBlockEntity = (BellBlockEntity) Minecraft.getInstance().level.getBlockEntity(position);
            return;
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(Identifier.tryParse("minecraft:blocks")).getSprite(Identifier.tryParse("minecraft:entity/bell/bell_body"));
        Direction facing = defaultState.getValue(BellBlock.FACING);
        BellAttachType attachment = defaultState.getValue(BellBlock.ATTACHMENT);

        int light = LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, position);

        float ticks = bellBlockEntity.ticks + Math.clamp(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true), 0.0f, 1.0f);
        Direction shakeDirection = bellBlockEntity.shaking ? bellBlockEntity.clickDirection : null;
        bellModel.setupAnim(new BellModel.State(ticks, shakeDirection));

        ModelPart bellBody = ((FwaModel) bellModel).getChildPart("bell_body");

        if (bellBlockEntity.ticks == 0) {
            float time = (float)(nowTick - this.startTick);
            float rot;
            if (time <= getAnimDuration()) rot = animatePlacement(nowTick);
            else rot = animateIdle(time);
            bellBody = rotateBell(bellBody, rot, facing, attachment);
        }

        SubmitNodeCollector submitNodeCollector = Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();
        submitNodeCollector.submitModelPart(bellBody, poseStack, ItemBlockRenderTypes.getRenderType(defaultState), light, OverlayTexture.NO_OVERLAY, sprite);
    }
}
