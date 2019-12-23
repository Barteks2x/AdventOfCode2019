package utils;

import java.util.Objects;

public class Vector3 {
    public final int x, y, z;

    public Vector3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 add(int x, int y, int z) {
        return new Vector3(x + this.x, y + this.y, z + this.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3 position3 = (Vector3) o;
        return x == position3.x &&
                y == position3.y &&
                z == position3.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    public Vector3 add(Vector3 v) {
        return add(v.x, v.y, v.z);
    }

    @Override
    public String toString() {
        return "<" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '>';
    }
}

