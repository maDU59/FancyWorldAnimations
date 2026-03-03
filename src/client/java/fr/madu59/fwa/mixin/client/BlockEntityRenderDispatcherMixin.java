package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
	private <E extends BlockEntity> void fwa$render(E blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, CallbackInfo info) {
		if (FancyWorldAnimationsClient.shouldCancelBlockEntityRendering(blockEntity.getBlockPos())) info.cancel();
	}
}
