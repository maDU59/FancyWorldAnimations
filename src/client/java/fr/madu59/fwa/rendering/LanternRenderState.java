package fr.madu59.fwa.rendering;

import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class LanternRenderState extends BlockEntityRenderState{
    public boolean hanging = true;
    public float tiltX = 0f;
    public float tiltZ = 0f;
    public float yaw = 0f;
    public float spin = 0f;
    public MovingBlockRenderState movingState;
    public int crumbleStage = -1;
    public long lastCrumbleParticleTime = 0L;
}