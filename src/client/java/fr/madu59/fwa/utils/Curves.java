package fr.madu59.fwa.utils;

public class Curves {

    public static <T> double ease(double progress, T type, boolean isForward) {
        return isForward? ease(progress, type):1-ease(1-progress, type);
    }

    public static <T> double ease(double progress, T type) {
        if (type instanceof Classic c) {
            switch (c) {
                default:
                    return progress;
            }
        }
        else if (type instanceof Door c) {
            switch (c) {
                case SPRINGY:
                    return wrap(1 + 2.70158 * Math.pow(progress - 1, 3) + 1.70158 * Math.pow(progress - 1, 2));
                // case ELASTIC:
                //     if (progress == 0) return 0;
                //     if (progress == 1) return 1;
                //     return wrap(Math.pow(2, -10 * progress) * Math.sin((progress * 10 - 0.75) * ((2 * Math.PI) / 3)) + 1);
                case DEFAULT:
                    return wrap(1 - Math.pow(1 - progress, 5));
                default:
                    return progress;
            }
        }
        else return progress;
    }

    public static <T> double unease(double progress, T type) {
        if (type instanceof Classic c) {
            switch (c) {
                default:
                    return progress;
            }
        }
        else if (type instanceof Door c) {
            switch (c) {
                case DEFAULT:
                    return 1 - Math.pow(1 - progress, 1/5);
                default:
                    return progress;
            }
        }
        else return progress;
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

    public static enum Classic {
        LINEAR
    }

    public static enum Door {
        DEFAULT,
        SPRINGY,
        LINEAR,
    }
}
