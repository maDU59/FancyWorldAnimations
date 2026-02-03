package fr.madu59.fwa.mixin;
import net.minecraft.world.level.block.entity.ChestLidController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;

@Mixin(ChestLidController.class)
public class ChestLidControllerMixin {

	@Shadow
	private boolean shouldBeOpen;

	@Inject(at = @At("RETURN"), method = "getOpenness", cancellable = true)
	private void getOpenness(float f, CallbackInfoReturnable<Float> info) {
		info.setReturnValue((float)Curves.ease(info.getReturnValue(), SettingsManager.CHEST_EASING.getValue(), shouldBeOpen));
	}
}