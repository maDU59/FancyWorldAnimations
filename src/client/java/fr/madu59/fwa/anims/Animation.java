package fr.madu59.fwa.anims;

import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.Curves;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class Animation {

    protected final BlockPos position;
    protected final double startTick;
    protected final boolean oldIsOpen;
    protected final boolean newIsOpen;
    protected final BlockState defaultState;
    protected boolean toRemove = false;
    protected double toRemoveTick = 0;
    protected boolean removalApproved = false;

    protected Boolean isLast;
    protected boolean needUpdate = true;

    public Animation(BlockPos position, BlockState defaultState, double startTick, boolean oldIsOpen, boolean newIsOpen) {
        this.position = position;
        this.defaultState = defaultState;
        this.startTick = startTick;
        this.oldIsOpen = oldIsOpen;
        this.newIsOpen = newIsOpen;
    }

    public boolean isUnique() {
        return true;
    }

    public void markForRemoval(){
        toRemove = true;
    }

    public void approveRemoval(double nowTick){
        toRemoveTick = nowTick;
        if(isForRemoval()) removalApproved = true;
    }

    public boolean isApprovedForRemoval(double nowTick){
        return isForRemoval() && removalApproved && (nowTick - toRemoveTick) >= .5;
    }

    public boolean isForRemoval(){
        return toRemove;
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T>> T getCurve() {
        return (T) Curves.Classic.LINEAR;
    }

    public boolean isEnabled(){
        return true;
    }

    public static boolean hasInfiniteAnimation(){
        return false;
    }

    public boolean hideOriginalBlock() {
        return true;
    }

    public boolean hideOriginalBlockEntity() {
        return true;
    }

    public BlockPos getPos() {
        return position;
    }

    public double getStartTick() {
        return startTick;
    }

    public boolean isFinished(double nowTick) {
        return nowTick - startTick >= getLifeSpan();
    }

    public double getAnimDuration() {
        return 0;
    }

    public double getLifeSpan(){
        return getAnimDuration();
    }

    public boolean needEndBatch(){
        return false;
    }

    public void setLast(boolean isLast){
        this.isLast = isLast;
    }

    public void needUpdate(){
        needUpdate = true;
    }

    public double getProgress(double nowTick) {
        return Math.clamp((nowTick - this.startTick) / getAnimDuration(), 0.0, 1.0);
    }

    public AABB getBoundingBox(){
        return new AABB(position);
    }

    public void render(AnimationRenderingContext context) {
    }
}
