package fr.madu59.fwa;

import java.util.ArrayList;
import java.util.Iterator;

import fr.madu59.fwa.anims.Animation;
import fr.madu59.fwa.mixin.client.SetSectionDirtyInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public class Animations extends ArrayList<Animation> {

    public Animations() {
        super();
    }
    
    public void removeAt(BlockPos blockPos) {
        synchronized (this) {
            this.removeIf(animation -> animation.getPos().equals(blockPos));
        }
    }

    public void clean(double nowTick) {
        synchronized (this) {
            Iterator<Animation> it = this.iterator();
            while (it.hasNext()) {
                Animation animation = it.next();
                if (animation.isFinished(nowTick)) {
                    it.remove();

                    BlockPos pos = animation.getPos();
                    ((SetSectionDirtyInvoker) Minecraft.getInstance().levelRenderer).fwa$setSectionDirty(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4, true);
                }
            }
        }
    }

    public boolean containsAt(BlockPos blockPos) {
        for (Animation animation : this) {
            if (animation.getPos().equals(blockPos)) {
                return true;
            }
        }
        return false;
    }

    public Animation getAt(BlockPos blockPos) {
        for (Animation animation : this) {
            if (animation.getPos().equals(blockPos)) {
                return animation;
            }
        }
        return null;
    }
}
