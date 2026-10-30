package fr.madu59.fwa.mixin.client.compat.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.rendering.AnimationRenderer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = BlockRenderContext.class,
        remap = false
)
public class BlockRenderContextMixin {

    @Shadow
    private BakedModel model;

    @Inject(
            method = {"update"},
            at = {@At("TAIL")}
    )
    private void fwa$update(BlockPos pos, BlockPos origin, BlockState state, BakedModel model, long seed, CallbackInfo ci) {
        if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
            this.model = AnimationRenderer.getInvisibleModel(this.model, state);
        }
    }

}
