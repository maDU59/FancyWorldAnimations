package fr.madu59.fwa.rendering;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.BlockBreakingRenderState;
import net.minecraft.core.BlockPos;

public class LanternBreakingRenderState extends BlockBreakingRenderState {

    public LanternBreakingRenderState(ClientLevel clientLevel, BlockPos blockPos, int i) {
        super(clientLevel, blockPos, i);
    }
}