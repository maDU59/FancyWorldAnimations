package fr.madu59.fwa.mixin.client.blockentity.ext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import fr.madu59.fwa.rendering.blockentity.BlockEntityRenderStateExt;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;

@Mixin(BlockEntityRenderState.class)
public class BlockEntityRenderStateExtMixin implements BlockEntityRenderStateExt {
    @Unique private BlockPos pos;

    @Override public void setPosition(BlockPos pos) { this.pos = pos; }
    @Override public BlockPos getPosition() { return this.pos; }
}
