package fr.madu59.fwa;

import java.util.Iterator;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.client.SetSectionDirtyInvoker;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class Animations{

    public final Long2ObjectMap<Animation> animations = new Long2ObjectOpenHashMap<>();

    public Animations() {
        super();
    }
    
    public void removeAt(BlockPos blockPos) {
        synchronized (this) {
            this.animations.remove(blockPos.asLong());
        }
    }

    public void clean(double nowTick) {
        synchronized (this) {
            Iterator<Animation> it = this.animations.values().iterator();
            while (it.hasNext()) {
                Animation animation = it.next();
                if (animation.isFinished(nowTick)) {
                    if (animation.isForRemoval()){
                        if(Minecraft.getInstance().levelRenderer.isSectionCompiledAndVisible(animation.getPos())) it.remove();
                    }
                    else{
                        animation.markForRemoval();
                        BlockPos pos = animation.getPos();
                        ((SetSectionDirtyInvoker) Minecraft.getInstance().levelRenderer).fwa$setSectionDirty(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4, true);
                    }
                }
            }
        }
    }

    public boolean containsAt(BlockPos blockPos) {
        return this.animations.containsKey(blockPos.asLong());
    }

    public Animation getAt(BlockPos blockPos) {
        return this.animations.getOrDefault(blockPos.asLong(), null);
    }

    public void add(BlockPos pos, Animation anim) {
        synchronized (this) {
            animations.put(pos.asLong(), anim);
        }
    }

    public boolean isEmpty(){
        return animations.isEmpty();
    }
}
