package fr.madu59.fwa.mixin.client.compat.vulkanmod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.minecraft.core.BlockPos;
import net.vulkanmod.render.chunk.build.renderer.BlockRenderer;

@Mixin(BlockRenderer.class)
public class VulkanMod {
    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    public void fwa$renderBlock(CallbackInfo ci, @Local(argsOnly = true) BlockPos pos){
        if(FancyWorldAnimationsClient.shouldCancelBlockRendering(pos)) ci.cancel();
    }
}
