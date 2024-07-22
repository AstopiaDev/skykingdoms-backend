package net.skykingdoms.backend.base.util.pair;

public final class FractionAccumulator {

    private double fraction;

    public int add(double value) {
        fraction += (value % 1.0D);

        if (fraction >= 1.0) {
            fraction--;
            value++;
        }

        return (int) value;
    }
}
