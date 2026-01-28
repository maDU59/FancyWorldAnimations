package fr.madu59.fwa.utils;

public class Curves {

    public static double ease(double progress, Type type) {
        switch (type) {
            case LINEAR:
                return progress;
            case DOOR:
                double p = progress - 1.0;
                return 1.0 + p * p * p;
            default:
                return progress;
        }
    }

    public static enum Type {
        LINEAR,
        DOOR
    }
}
