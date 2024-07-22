package net.skykingdoms.backend.base.util.pair;

import lombok.NonNull;

public class NumberPair extends Pair<Number, Number> {

    public NumberPair(final @NonNull Number first,
                      final @NonNull Number second) {
        super(first, second);
    }

    public static NumberPair newImmutablePair(final @NonNull Number a,
                                              final @NonNull Number b) {
        return new Immutable(a, b);
    }

    public void setFirstByte(final byte value) {
        setFirst(value);
    }

    public void setSecondByte(final byte value) {
        setSecond(value);
    }

    public void setFirstShort(final short value) {
        setFirst(value);
    }

    public void setSecondShort(final short value) {
        setSecond(value);
    }

    public void setFirstInt(final int value) {
        setFirst(value);
    }

    public void setSecondInt(final int value) {
        setSecond(value);
    }

    public void setFirstLong(final long value) {
        setFirst(value);
    }

    public void setSecondLong(final long value) {
        setSecond(value);
    }

    public void setFirstFloat(final float value) {
        setFirst(value);
    }

    public void setSecondFloat(final float value) {
        setSecond(value);
    }

    public void setFirstDouble(final double value) {
        setFirst(value);
    }

    public void setSecondDouble(final double value) {
        setSecond(value);
    }

    public byte getFirstByte() {
        return isFirstNull() ? 0 : getFirst().byteValue();
    }

    public byte getSecondByte() {
        return isSecondNull() ? 0 : getSecond().byteValue();
    }

    public short getFirstShort() {
        return isFirstNull() ? 0 : getFirst().shortValue();
    }

    public short getSecondShort() {
        return isSecondNull() ? 0 : getSecond().shortValue();
    }

    public int getFirstInt() {
        return isFirstNull() ? 0 : getFirst().intValue();
    }

    public int getSecondInt() {
        return isSecondNull() ? 0 : getSecond().intValue();
    }

    public long getFirstLong() {
        return isFirstNull() ? 0 : getFirst().longValue();
    }

    public long getSecondLong() {
        return isSecondNull() ? 0 : getSecond().longValue();
    }

    public float getFirstFloat() {
        return isFirstNull() ? 0 : getFirst().floatValue();
    }

    public float getSecondFloat() {
        return isSecondNull() ? 0 : getSecond().floatValue();
    }

    public double getFirstDouble() {
        return isFirstNull() ? 0 : getFirst().doubleValue();
    }

    public double getSecondDouble() {
        return isSecondNull() ? 0 : getSecond().doubleValue();
    }

    @Override
    public NumberPair asImmutable() {
        return new Immutable(getFirst(), getSecond());
    }

    @Override
    public NumberPair asMutable() {
        return this;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    private static class Immutable extends NumberPair {
        public Immutable(final @NonNull Number first, final @NonNull Number second) {
            super(first, second);
        }

        @Override
        public boolean isImmutable() {
            return true;
        }

        @Override
        public void setFirst(final @NonNull Number first) {
            throw new IllegalStateException("Cannot modify immutable pair");
        }

        @Override
        public void setSecond(final @NonNull Number second) {
            throw new IllegalStateException("Cannot modify immutable pair");
        }

        @Override
        public NumberPair asImmutable() {
            return this;
        }

        @Override
        public NumberPair asMutable() {
            return new NumberPair(getFirst(), getSecond());
        }

    }
}
