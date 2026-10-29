package fr.madu59.fwa.mixin.client.blockentity.renderer.bell.ext;

import fr.madu59.fwa.rendering.blockentity.bell.BellRenderStateExt;
import net.minecraft.client.renderer.blockentity.state.BellRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.core.BlockPos;

@Mixin(BellRenderState.class)
public abstract class BellRenderStateMixin implements BellRenderStateExt {
    @Unique private BlockPos pos;

    @Override public void fwa$setPosition(BlockPos pos) { this.pos = pos; }
    @Override public BlockPos fwa$getPosition() { return this.pos; }
}
