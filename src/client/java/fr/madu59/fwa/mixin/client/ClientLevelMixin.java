package fr.madu59.fwa.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import fr.madu59.fwa.FancyWorldAnimationsClient;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
	@Inject(at = @At("HEAD"), method = "sendBlockUpdated")
	private void sendBlockUpdated(BlockPos blockPos, BlockState oldState, BlockState newState, int i, CallbackInfo info) {
		FancyWorldAnimationsClient.onBlockUpdate(blockPos.immutable(), oldState, newState);
	}
}