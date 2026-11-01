package fr.madu59.fwa.mixin.compat.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
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
    private void fwa$update(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local(argsOnly = true) BakedModel model, @Local(argsOnly = true) BlockState state) {
        if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
            this.model = AnimationRenderer.getInvisibleModel(model, state);
        }
    }

}
