package fr.madu59.fwa.mixin.blockentity.renderer.lectern.ext;

import fr.madu59.fwa.rendering.blockentity.lectern.LecternRenderStateExt;
import net.minecraft.client.renderer.blockentity.state.LecternRenderState;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LecternRenderState.class)
public abstract class LecternRenderStateMixin implements LecternRenderStateExt {
    @Unique private BlockPos pos;

    @Override public void fwa$setPosition(BlockPos pos) { this.pos = pos; }
    @Override public BlockPos fwa$getPosition() { return this.pos; }
}
