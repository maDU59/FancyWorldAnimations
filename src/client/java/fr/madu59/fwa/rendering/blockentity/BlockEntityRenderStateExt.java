package fr.madu59.fwa.rendering.blockentity;

import net.minecraft.core.BlockPos;

public interface BlockEntityRenderStateExt {
    BlockPos getPosition();
    void setPosition(BlockPos pos);
}