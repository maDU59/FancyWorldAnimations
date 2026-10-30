package fr.madu59.fwa.mixin.client.blockentity.renderer.sign.ext;

import fr.madu59.fwa.api.animations.HangingSwing;
import fr.madu59.fwa.rendering.blockentity.sign.SignRenderStateExt;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SignRenderState.class)
public abstract class SignRenderStateMixin implements SignRenderStateExt {
        @Unique
        private HangingSwing swing = null;
        @Unique
        private int direction = -1;

        @Override
        public void fwa$setSwing(HangingSwing swing) {
            this.swing = swing;
        }

        @Override
        public HangingSwing fwa$getSwing() {
            return this.swing;
        }

        @Override
        public int fwa$getDirection() {
            return direction;
        }

        @Override
        public void fwa$setDirection(int direction) {
            this.direction = direction;
        }
}
