package fr.madu59.fwa.anims;

import com.mojang.blaze3d.vertex.VertexConsumer;

import fr.madu59.fwa.rendering.AnimationRenderingContext;
import fr.madu59.fwa.utils.Curves;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class Animation {

    protected final BlockPos position;
    protected final double startTick;
    protected final boolean oldIsOpen;
    protected final boolean newIsOpen;
    protected final BlockState defaultState;
    protected final BlockState oldState;
    protected final BlockState newState;
    protected final RenderType renderType;
    protected double toRemoveTick = 0.0;
    protected boolean toRemove = false;
    protected boolean removalApproved = false;

    protected Boolean isLast;
    protected boolean needUpdate = true;

    public Animation(BlockPos position, double startTick, boolean oldIsOpen, boolean newIsOpen, BlockState oldState, BlockState newState) {
        this.position = position;
        this.defaultState = getDefaultState(newState);
        this.startTick = startTick;
        this.oldIsOpen = oldIsOpen;
        this.newIsOpen = newIsOpen;
        this.oldState = oldState;
        this.newState = newState;
        this.renderType = getRenderType(newState);
    }

    public RenderType getRenderType(BlockState state){
        RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(state);
      return renderType == RenderType.translucent() ? RenderType.translucentMovingBlock() : RenderType.cutoutMipped();
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

    public boolean isEnabled(BlockState state){
        return true;
    }

    public boolean hasInfiniteAnimation(){
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
        double duration = getAnimDuration();
        if (duration <= 0) return 1.0;
        return Math.clamp((nowTick - this.startTick) / duration, 0.0, 1.0);
    }

    public AABB getBoundingBox(){
        return new AABB(position);
    }

    protected BlockState getDefaultState(BlockState state){
        return state;
    }

    public BlockState getDefaultState(){
        return defaultState;
    }

    public boolean isRendering(){
        return true;
    }

    public void tick(double nowTick) {
    }

    public void render(AnimationRenderingContext context) {
    }

    protected VertexConsumer getBuffer(AnimationRenderingContext context){
        return context.getBufferSource().getBuffer(this.renderType);
    }

    protected VertexConsumer getBuffer(AnimationRenderingContext context, RenderType renderType){
        return context.getBufferSource().getBuffer(renderType);
    }
    
    public int getLight(){
        return getLight(position, newState);
    }

    public int getLight(BlockPos pos, BlockState state){
        return LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, state, pos);
    }

    public int getLight(BlockPos pos){
        return LevelRenderer.getLightColor((BlockAndTintGetter) Minecraft.getInstance().level, pos);
    }

    public int getRelativeLight(Direction dir){
        return getLight(position.relative(dir));
    }
}
