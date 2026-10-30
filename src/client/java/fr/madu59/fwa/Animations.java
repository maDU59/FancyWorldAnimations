package fr.madu59.fwa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.client.SetSectionDirtyInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;

public class Animations{

    public final Map<BlockPos, Animation> animations = new ConcurrentHashMap<>();
    
    public void removeAt(BlockPos blockPos) {
        this.animations.remove(blockPos);
    }

    public void removeSafeAt(BlockPos blockPos) {
        Animation animation = getAt(blockPos);
        if(animation == null) return;
        LevelExtractor levelExtractor = Minecraft.getInstance().levelExtractor;
        animation.markForRemoval();
        ((SetSectionDirtyInvoker) levelExtractor).fwa$setSectionDirty(blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4, true);
    }

    public boolean containsAt(BlockPos blockPos) {
        return this.animations.containsKey(blockPos);
    }

    public Animation getAt(BlockPos blockPos) {
        return this.animations.get(blockPos);
    }

    public void add(BlockPos pos, Animation anim) {
        animations.put(pos, anim);
    }

    public boolean isEmpty(){
        return animations.isEmpty();
    }

    public void clearModelCache(){
        for(Animation animation : this.animations.values()){
            animation.clearModelCache();
        }
    }
}
