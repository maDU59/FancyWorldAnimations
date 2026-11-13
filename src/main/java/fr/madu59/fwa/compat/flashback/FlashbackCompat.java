package fr.madu59.fwa.compat.flashback;

import fr.madu59.fwa.compat.ModCompat;

import java.lang.reflect.Method;

public final class FlashbackCompat{
    private static Method getVisualMillis;
    private static Method isExporting;

    static{
        if (ModCompat.isFlashbackLoaded()) {
            try{
                Class<?> flashbackClass = Class.forName("com.moulberry.flashback.Flashback");
                getVisualMillis = flashbackClass.getMethod("getVisualMillis");
                isExporting = flashbackClass.getMethod("isExporting");
            }catch(Exception e){
                isExporting = null;
                getVisualMillis = null;
            }
        }
        else{
            isExporting = null;
            getVisualMillis = null;
        }
    }

    public static double getPartialTick(double defaultValue){
        if (isExporting == null || getVisualMillis == null) return defaultValue;
        try{
            if(Boolean.TRUE.equals(isExporting.invoke(null))){
                Object millis = getVisualMillis.invoke(null);
                if(millis instanceof Number number){
                    return number.doubleValue() / 50.0;
                }
                return defaultValue;
            }
            else{
                return defaultValue;
            }
        }catch(Exception e){
            return defaultValue;
        }
    }
}
