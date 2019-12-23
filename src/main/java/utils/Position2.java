package utils;

import java.util.Objects;

public class Position2 {
    public final int x, y;

    public Position2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position2 add(int x, int y) {
        return new Position2(x + this.x, y + this.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position2 position = (Position2) o;
        return x == position.x &&
                y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Position2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
