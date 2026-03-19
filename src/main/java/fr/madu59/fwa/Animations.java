package fr.madu59.fwa;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.SetSectionDirtyInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;

public class Animations{

    public final Map<BlockPos, Animation> animations = new ConcurrentHashMap<>();

    public Animations() {
        super();
    }
    
    public void removeAt(BlockPos blockPos) {
        this.animations.remove(blockPos);
    }

    public void clean(double nowTick) {
        ClientLevel level = Minecraft.getInstance().level;
        LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        Iterator<Animation> it = this.animations.values().iterator();
        while (it.hasNext()) {
            Animation animation = it.next();
            if (animation.isFinished(nowTick)) {
                if (animation.isForRemoval()){
                    if(!(animation.hideOriginalBlock() || animation.hideOriginalBlockEntity()) || animation.isApprovedForRemoval(nowTick)) {
                        it.remove();
                    }
                }
                else{
                    animation.markForRemoval();
                    BlockPos pos = animation.getPos();
                    ((SetSectionDirtyInvoker) levelRenderer).fwa$setSectionDirty(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4, true);
                }
            }
            if(!level.isLoaded(animation.getPos())){
                it.remove();
            }
        }
    }

    public boolean containsAt(BlockPos blockPos) {
        return this.animations.containsKey(blockPos);
    }

    public Animation getAt(BlockPos blockPos) {
        return this.animations.getOrDefault(blockPos, null);
    }

    public void add(BlockPos pos, Animation anim) {
        animations.put(pos, anim);
    }

    public boolean isEmpty(){
        return animations.isEmpty();
    }
}