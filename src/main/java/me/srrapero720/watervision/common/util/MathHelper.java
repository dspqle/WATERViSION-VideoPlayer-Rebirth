package me.srrapero720.watervision.common.util;

/**
 * Small math helpers formerly provided by WaterMedia 2.x {@code MathAPI}, which was removed in
 * WaterMedia 3.0. Kept local so WaterVision does not depend on the old API surface.
 */
public final class MathHelper {
    private MathHelper() {}

    /**
     * Quadratic ease-in interpolation between {@code start} and {@code end} at an unclamped
     * time {@code t} in [0, 1].
     */
    public static double easeIn(final double start, final double end, final double t) {
        return start + (end - start) * t * t;
    }

    /**
     * Scales a value from one range onto another, equivalent to the old {@code MathAPI.scaleTempo}.
     * When {@code fromMax == fromMin} the result collapses to {@code toMin} to avoid dividing by zero.
     */
    public static double scaleTempo(final double fromMin, final double fromMax, final double value) {
        if (fromMax == fromMin) return fromMin;
        return (value - fromMin) / (fromMax - fromMin);
    }

    /**
     * Packs ARGB components into a single int, equivalent to the old {@code MathAPI.argb}.
     */
    public static int argb(final int alpha, final int red, final int green, final int blue) {
        return ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
    }
}
