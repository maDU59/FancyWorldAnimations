package fr.madu59.fwa.mixin.client;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.block.entity.ChestLidController;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import fr.madu59.fwa.config.SettingsManager;
import fr.madu59.fwa.utils.Curves;

@Mixin(ChestLidController.class)
public abstract class ChestLidControllerMixin {

	@Shadow
	private boolean shouldBeOpen;

	@ModifyReturnValue(at = @At("RETURN"), method = "getOpenness")
	private float fwa$getOpenness(float original) {
		return (float) Curves.ease(original, SettingsManager.CHEST_EASING.getValue(), shouldBeOpen);
	}
}