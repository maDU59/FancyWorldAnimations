package fr.madu59.fwa.mixin.client.blockentity.renderer.sign.ext;

import fr.madu59.fwa.rendering.blockentity.sign.HangingSignRenderStateExt;
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HangingSignRenderState.class)
public abstract class HangingSignRenderStateMixin implements HangingSignRenderStateExt {
        @Unique
        private float swing;

        @Override
        public void fwa$setSwing(float swing) {
            this.swing = swing;
        }

        @Override
        public float fwa$getSwing() {
            return this.swing;
        }
}
