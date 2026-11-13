package fr.madu59.fwa.compat.flashback;

import com.moulberry.flashback.Flashback;

public final class FlashbackCompat{
    public static double getPartialTick(double defaultValue){
        if (!Flashback.isExporting()) return defaultValue;
        return Flashback.getVisualMillis() / 50.0;
    }
}
