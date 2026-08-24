package fr.madu59.fwa.anims;

import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.bell.BellModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BellAnimation extends Animation{

    private final Minecraft client = Minecraft.getInstance();
    public final BellModel bellModel;
    private final float hash;
    private final Direction facing;
    private final BellAttachType attachment;
    
    public BellAnimation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        super(position, startTick, oldIsOpen, newIsOpen, oldState, newState);
        this.bellModel = new BellModel(client.getEntityModels().bakeLayer(ModelLayers.BELL));
        this.hash = position.hashCode();

        facing = defaultState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        attachment = defaultState.getValue(BlockStateProperties.BELL_ATTACHMENT);
    }

    @Override
    public double getAnimDuration() {
        return 50 / SettingsManager.BELL_SPEED.getValue();
    }

    @Override
    public boolean isEnabled(BlockState state){
        return SettingsManager.BELL_STATE.getValue();
    }

    @Override
    public boolean hideOriginalBlock() {
        return false;
    }

    @Override
    public boolean hideOriginalBlockEntity() {
        return false;
    }

    public boolean hasInfiniteAnimation(){
        return SettingsManager.BELL_INFINITE.getValue();
    }

    @Override
    public double getLifeSpan(){
        return !hasInfiniteAnimation()? getAnimDuration() : Double.MAX_VALUE;
    }

    public float animateIdle(float time){
        float uniqueOffset = (float)(hash % 100);
        time += uniqueOffset;
        float slowWave = (float) Math.sin(time * 0.1f) * 0.05f;

        float fastWave = (float) Math.sin(time * 0.25f) * 0.02f;

        return ((slowWave + fastWave) / 2f) * Math.clamp(time - (float)getAnimDuration(), 0f, 5f)/5f;
    }

    public float animatePlacement(double nowTick){
        double progress = getProgress(nowTick);
        return Mth.sin(progress * getAnimDuration() / 3.1415927) / (float)(4.0 + progress * getAnimDuration() / 3.0);
    }

    public void rotateBell(ModelPart bellBody, float rot){
        rotateBell(bellBody, rot, facing, attachment);
    }

    public void rotateBell(ModelPart bellBody, float rot, Direction facing, BellAttachType attachment){
        float f = 0.0F;
        float g = 0.0F;
        switch (facing) {
            case NORTH:
                f = rot;
                break;
            case SOUTH:
                f = -rot;
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
    }
}
