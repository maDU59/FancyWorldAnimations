package fr.madu59.fwa.utils;

public class Curves {

    public static double ease(double t, Enum<?> type, boolean isForward) {
        return isForward? ease(t, type):1-ease(1-t, type);
    }

    public static double ease(double t, Enum<?> type) {
        if (type instanceof Classic c) {
            switch (c) {
                case EASE_IN_OUT_CUBIC:
                    return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
                default:
                    return t;
            }
        }
        else if (type instanceof Door c) {
            switch (c) {
                case SPRINGY:
                    return wrap(1 + 2.70158 * Math.pow(t - 1, 3) + 1.70158 * Math.pow(t - 1, 2));
                // case ELASTIC:
                //     if (t == 0) return 0;
                //     if (t == 1) return 1;
                //     return wrap(Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * ((2 * Math.PI) / 3)) + 1);
                case DEFAULT:
                    return wrap(1 - Math.pow(1 - t, 5));
                default:
                    return t;
            }
        }
        else return t;
    }

    public static double unease(double t, Enum<?> type) {
        if (type instanceof Classic c) {
            switch (c) {
                default:
                    return t;
            }
        }
        else if (type instanceof Door c) {
            switch (c) {
                case DEFAULT:
                    return 1 - Math.pow(1 - t, 1/5);
                default:
                    return t;
            }
        }
        else return t;
    }

    public static float smooth(float t){
        return t * t * (3.0F - 2.0F * t);
    }

    private static double wrap(double value){
        if (value >= 0.0 && value <= 1.0) {
            return value;
        }
        else if (value < 0.0) {
            return - value;
        } else {
            return 1.0 - (value - 1.0);
        }
    }

    public static double getSpeedCoeff(Speed speed){
        switch(speed){
            case FAST: return 1.5;
            case SLOW: return 0.65;
            default: return 1;
        }
    }

    public static enum Classic {
        LINEAR,
        EASE_IN_OUT_CUBIC
    }

    public static enum Door {
        DEFAULT,
        SPRINGY,
        LINEAR,
    }

    public static enum Speed{
        SLOW,
        DEFAULT,
        FAST,
    }
}
