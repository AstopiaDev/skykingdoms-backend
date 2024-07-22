package net.skykingdoms.backend.base.util.math;

import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.base.util.pair.NumberPair;

@UtilityClass
public class MathUtil {

    //===========================================
    private final double[] SINE = new double[361];
    private final double[] COSINE = new double[361];

    private final String[] ROMANS = new String[]{
            "", "I", "II", "III", "IV", "V",
            "VI", "VII", "VIII", "IX", "X"
    };
    //===========================================

    public double getParametrized(double x) {
        return getParametrized(x, 2.0);
    }

    public static int round(double num) {
        return (int) (num + 0.5D);
    }

    public static int ceil(double num) {
        int floor = (int) num;

        return (double) floor == num ? floor
                : floor + (int) (~Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int divideCeil(int x, int y) {
        return x / y + (x % y > 0 ? 1 : 0);
    }

    public static float square(float num) {
        return num * num;
    }

    public static int square(int num) {
        return num * num;
    }

    public static double square(double num) {
        return num * num;
    }

    public double getParametrized(double x, double alpha) {
        if (x < 0 || x > 1) {
            throw new IllegalArgumentException("x not in 0..1: " + x);
        }

        double pow = Math.pow(x, alpha);
        return pow / (pow + Math.pow(1 - x, alpha));
    }

    public double getSin(double x) {
        if (x < 0 || x > 1) {
            throw new IllegalArgumentException("x not in 0..1: " + x);
        }

        return 0.5 * (Math.sin(Math.PI * (x - 0.5)) + 1);
    }

    public String getRoman(int i) {
        if (i >= ROMANS.length) {
            throw new IllegalArgumentException("Can't determine roman-like number for " + i);
        }

        return ROMANS[i];
    }


    static {
        for (int i = 0; i <= 360; i++) {
            SINE[i] = Math.sin(Math.toRadians(i));
            COSINE[i] = Math.cos(Math.toRadians(i));
        }
    }

    public short pair2b(NumberPair pair) {
        return pair2b(pair.getFirstByte(), pair.getSecondByte());
    }

    public short pair2b(byte x, byte y) {
        return (short) ((x & 0xFF) << 8 | y & 0xFF);
    }

    public NumberPair pair2b(short i) {
        return NumberPair.newImmutablePair(
                (byte) ((i >> 8) & 0xFF),
                (byte) (i & 0xFF)
        );
    }

    public int pair4b(NumberPair pair) {
        return pair4b(pair.getFirstShort(), pair.getSecondShort());
    }

    public int pair4b(short x, short y) {
        return (int) x << 16 | y & 0xFFFF;
    }

    public NumberPair pair4b(int i) {
        return NumberPair.newImmutablePair(
                (short) (i >> 16),
                (short) i
        );
    }

    public long pair8b(int i, int j) {
        return (long) i << 32 | j & 0xFFFFFFFFL;
    }

    public long pair8b(NumberPair pair) {
        return pair8b(pair.getFirstInt(), pair.getSecondInt());
    }

    public NumberPair pair8b(long l) {
        return NumberPair.newImmutablePair(
                (int) (l >> 32),
                (int) l
        );
    }

    public double cos(int angle) {
        return angle < 0 ? -COSINE[Math.abs(angle) % 360] : COSINE[angle % 360];
    }

    public double cos(float angle) {
        return cos((int) angle);
    }

    public double sin(int angle) {
        return angle < 0 ? -SINE[Math.abs(angle) % 360] : SINE[angle % 360];
    }

    public double sin(float angle) {
        return sin((int) angle);
    }

    public double round(double d, int places) {
        double scale = Math.pow(10, places);

        return round(d * scale) / scale;
    }

}
