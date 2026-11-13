package fr.madu59.fwa.rendering.blockentity.sign;

import fr.madu59.fwa.api.animations.HangingSwing;

public interface HangingSignRenderStateExt {
    void fwa$setSwing(HangingSwing swing);
    HangingSwing fwa$getSwing();

    int fwa$getDirection();
    void fwa$setDirection(int direction);
}
