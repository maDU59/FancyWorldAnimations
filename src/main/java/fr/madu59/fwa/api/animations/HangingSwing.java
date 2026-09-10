package fr.madu59.fwa.api.animations;

/*
 * HangingSwing class carries the swing of a hanging block in degrees. Cleared before each
 * call, so any field the driver leaves alone is zero.
 */
public class HangingSwing {

    public float tiltX;

    public float tiltZ;

    public float spin;

    public void set(float tiltX, float tiltZ, float spin){
        this.tiltX = tiltX;
        this.tiltZ = tiltZ;
        this.spin = spin;
    }
}
