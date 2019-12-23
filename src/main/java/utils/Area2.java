package utils;

import java.util.Iterator;

public class Area2 implements Iterable<Position2> {
    private final Position2 start;
    private final Position2 end;

    public Area2(Position2 start, Position2 end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Iterator<Position2> iterator() {
        return new Iterator<Position2>() {
            int x = start.x;
            int y = start.y;

            @Override
            public boolean hasNext() {
                return x <= end.x && y <= end.y;
            }

            @Override
            public Position2 next() {
                Position2 ret = new Position2(x, y);
                x++;
                if (x > end.x) {
                    x = start.x;
                    y++;
                }
                return ret;
            }
        };
    }
}
