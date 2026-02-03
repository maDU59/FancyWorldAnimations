package fr.madu59.fwa.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RenderSectionRegion.class)
public class RenderSectionRegionMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void hideAnimatedBlocks(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
            cir.setReturnValue(Blocks.AIR.defaultBlockState());
        }
    }
}
