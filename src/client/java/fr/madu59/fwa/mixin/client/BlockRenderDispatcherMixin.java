package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RenderChunkRegion.class)
public class BlockRenderDispatcherMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void fwa$hideAnimatedBlocks(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
