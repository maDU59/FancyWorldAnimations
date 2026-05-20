package fr.madu59.fwa.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fr.madu59.fwa.FancyWorldAnimationsClient;
import fr.madu59.fwa.config.SettingsManager;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

@Mixin(targets = "net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer$ShulkerBoxModel")
public abstract class ShulkerBoxModelMixin {

    double timer = 0;
    double timerStart = 0;

    @Redirect(method = "animate",
        at = @At(
            value = "INVOKE", 
            target = "Lnet/minecraft/client/model/geom/ModelPart;setPos(FFF)V"
        )
    )
    private void fwa$animate(ModelPart lid, float x, float y, float z, ShulkerBoxBlockEntity shulkerBoxBlockEntity, float f) {
        float progress = shulkerBoxBlockEntity.getProgress(f);
        System.out.println(progress);
        if (progress >= 1.0f && SettingsManager.SHULKERBOX_STATE.getValue()) {
            if(timerStart == 0) timerStart = FancyWorldAnimationsClient.getPartialTick();
            timer = FancyWorldAnimationsClient.getPartialTick() - timerStart;
            lid.setPos(x, y + (float)((Math.sin(timer * 0.1) - 0.5) * 0.3 * Math.clamp(timer * 0.04, 0.0f, 1.0f)), z);
        }
        else{
            timerStart = 0;
            lid.setPos(x, y, z);
        }
    }
}
