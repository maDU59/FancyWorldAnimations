package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

@Mixin(ShulkerBoxRenderer.class)
public abstract class ShulkerBoxModelMixin {

    double timerStart = 0.0;

    @Redirect(method = "render",
        at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/client/model/geom/ModelPart;setPos(FFF)V"
        )
    )
    private void fwa$animate(ModelPart lid, float x, float y, float z, ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f) {

        float progress = shulkerBoxBlockEntity.getProgress(f);
        
        if (progress == 1.0f && SettingsManager.SHULKERBOX_STATE.getValue()) {
            if(timerStart == 0) timerStart = FancyWorldAnimationsClient.getPartialTick();
            double timer = FancyWorldAnimationsClient.getPartialTick() - timerStart;
            lid.setPos(x, y + (float)((Math.sin(timer * 0.1 * SettingsManager.SHULKERBOX_SPEED.getValue()) - 0.5) * 0.3 * Math.min(Math.max(timer * 0.04, 0.0f), 1.0f)), z);
        }
        else{
            if(progress != 0.0f) timerStart = 0.0;
            lid.setPos(x, y, z);
        }
    }
}
