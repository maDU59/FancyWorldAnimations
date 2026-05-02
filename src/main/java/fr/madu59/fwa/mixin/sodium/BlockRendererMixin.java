package fr.madu59.fwa.mixin.sodium;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.rendering.RenderHelper;

@Mixin(
   targets = {"net/caffeinemc/sodium/client/render/chunk/compile/pipeline/BlockRenderer", "net/caffeinemc/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer", "me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderer"},
   remap = false
)
public class BlockRendererMixin {

    @ModifyVariable(
        method = {"renderModel"},
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private BlockStateModel fwa$render(BlockStateModel originalModel, BlockStateModel model, BlockState state, BlockPos pos, BlockPos origin) {
        return FancyWorldAnimationsClient.shouldCancelBlockRendering(pos) ? RenderHelper.getInvisibleModel(originalModel) : originalModel;
    }

}
