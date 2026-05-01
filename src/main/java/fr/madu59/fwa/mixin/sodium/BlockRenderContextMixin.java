package fr.madu59.fwa.mixin.sodium;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.rendering.RenderHelper;

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
   private void fwa$update(CallbackInfo ci, @Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local(argsOnly = true) BakedModel model) {
      if (FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) {
         this.model = RenderHelper.getInvisibleModel(model);
      }
   }
}
