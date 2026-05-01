package fr.madu59.fwa.mixin.client.sodium;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.rendering.RenderHelper;

@Environment(EnvType.CLIENT)
@Mixin(
   targets = {"me/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext"},
   remap = false
)
public abstract class BlockRenderContextMixin {
   @Shadow
   private BlockState state;
   @Shadow
   private BakedModel model;

   @Inject(
      method = {"update"},
      at = {@At("TAIL")}
   )
   private void swinginglanterns$microScaleStaticLantern(BlockPos pos, BlockPos origin, BlockState state, BakedModel model, long seed, CallbackInfo ci) {
      if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
         this.model = RenderHelper.getInvisibleModel(this.model);
      }
   }
}
