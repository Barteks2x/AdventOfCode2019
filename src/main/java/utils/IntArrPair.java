package utils;

import java.util.Arrays;

public class IntArrPair {
    final int[] a;
    final int[] b;

    public IntArrPair(int[] a, int[] b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntArrPair that = (IntArrPair) o;
        return Arrays.equals(a, that.a) &&
                Arrays.equals(b, that.b);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(a);
        result = 31 * result + Arrays.hashCode(b);
        return result;
    }
}
