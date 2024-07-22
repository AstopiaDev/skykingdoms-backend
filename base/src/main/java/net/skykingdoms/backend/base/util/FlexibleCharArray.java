package net.skykingdoms.backend.base.util;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * @author Unidentified Person
 */
public final class FlexibleCharArray {

    private char[] value;

    private int length;
    private int position;

    public FlexibleCharArray(char[] value) {
        this.value = value;
        this.length = value.length;
    }

    public FlexibleCharArray(int capacity) {
        value = new char[capacity];
    }

    public FlexibleCharArray() {
        this(16);
    }

    public int length() {
        return length;
    }

    public void ensure(int n) {
        if (length + n > value.length) {
            value = Arrays.copyOf(value, Math.max(n + length, value.length * 2));
        }
    }

    public int position() {
        return position;
    }

    public void position(int position) {
        this.position = position;
    }

    public int read() {
        return position == length ? -1 : value[position++];
    }

    public boolean hasRemaining() {
        return position != length;
    }

    public String substring(int begin, int end) {
        return new String(value, begin, end - begin);
    }

    public void write(int i) {
        write((char) i);
    }

    public void write(char ch) {
        ensure(1);
        value[length++] = ch;
    }

    public void write(CharSequence sequence) {
        ensure(sequence.length());

        for (int i = 0; i < sequence.length(); i++) {
            value[length++] = sequence.charAt(i);
        }
    }

    public void write(char[] ch) {
        write(ch, 0, ch.length);
    }

    public void write(char[] ch, int off, int len) {
        ensure(ch.length);

        System.arraycopy(ch, off, value, length, len);
        length += len;
    }

    public void reset() {
        length = 0;
    }

    public byte[] getBytes(Charset charset) {
        return toString().getBytes(charset);
    }

    public String toString() {
        return new String(value, 0, length);
    }

}
