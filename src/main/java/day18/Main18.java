package day18;

import utils.Pair;
import utils.Position2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main18 {
    public static void main(String[] args) throws IOException {
        long startTime = System.nanoTime();
        byte[][] map = Files.readAllLines(Paths.get("run/in18.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("//"))
                .map(s -> s.getBytes(StandardCharsets.US_ASCII))
                .toArray(byte[][]::new);

        Position2 start = findStart(map);

        int keysToCollect = findAllKeys(map);

        map[start.x][start.y] = '.';
        int len = findShortestPath(start, map, keysToCollect);
        System.out.println("Path1 " + len);

        map[start.x][start.y] = '#';
        map[start.x + 1][start.y] = '#';
        map[start.x - 1][start.y] = '#';
        map[start.x][start.y + 1] = '#';
        map[start.x][start.y - 1] = '#';

        long positions = packPositions(start.add(1, 1), start.add(-1, -1), start.add(-1, 1), start.add(1, -1));

        System.out.println("(perf) Call count = " + count);
        reachableMap.clear(); // we changed the map
        len = findShortestPath2(positions, map, keysToCollect);
        System.out.println("Path2 " + len);
        System.out.println("(perf) Call count2 = " + count2);
        System.out.println(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime) * 0.001);
    }

    private static long packPositions(Position2 q1, Position2 q2, Position2 q3, Position2 q4) {
        long q1p = (q1.x & 0xFF) | ((q1.y & 0xFF) << 8);
        long q2p = (q2.x & 0xFF) | ((q2.y & 0xFF) << 8);
        long q3p = (q3.x & 0xFF) | ((q3.y & 0xFF) << 8);
        long q4p = (q4.x & 0xFF) | ((q4.y & 0xFF) << 8);
        return q1p | q2p << 16 | q3p << 32 | q4p << 48;
    }

    private static Position2 unpack(long pos) {
        byte x = (byte) pos;
        byte y = (byte) (pos >> 8);
        return new Position2(x, y);
    }

    private static Map<Pair<Integer, Long>, Integer> distMap2 = new HashMap<>();

    static AtomicInteger count2 = new AtomicInteger();

    private static int findShortestPath2(long start, byte[][] map, int keysToCollect) {
        if (keysToCollect == 0) {
            return 0;
        }
        Pair<Integer, Long> pair = new Pair<>(keysToCollect, start);
        Integer precomputed = distMap2.get(pair);
        if (precomputed != null) return precomputed;

        count2.incrementAndGet();
        Position2 p1 = unpack(start);
        Position2 p2 = unpack(start >>> 16);
        Position2 p3 = unpack(start >>> 32);
        Position2 p4 = unpack(start >>> 48);

        int bestSoFar = 999999999;
        Position2[] spa = {p1, p2, p3, p4};
        for (int i = 0; i < spa.length; i++) {
            Position2 sp = spa[i];
            Map<Position2, Integer> reachable = findAllReachable(sp, map, keysToCollect);

            for (Map.Entry<Position2, Integer> entry : reachable.entrySet()) {
                Position2 pos = entry.getKey();
                byte c = map[pos.x][pos.y];
                if ((keysToCollect & (1 << getKeyId(c))) == 0) {
                    continue;
                }
                int dist = entry.getValue();

                int toCollect = keysToCollect;
                if (Character.isLowerCase(c)) {
                    toCollect &= ~(1 << getKeyId(c));
                }
                long nsp = packPositions(
                        i == 0 ? pos : spa[0],
                        i == 1 ? pos : spa[1],
                        i == 2 ? pos : spa[2],
                        i == 3 ? pos : spa[3]
                );
                dist += findShortestPath2(nsp, map, toCollect);

                if (dist < bestSoFar) {
                    bestSoFar = dist;
                }
            }
        }
        distMap2.put(pair, bestSoFar);
        return bestSoFar;
    }


    private static Map<Pair<Integer, Position2>, Integer> distMap = new HashMap<>();

    static AtomicInteger count = new AtomicInteger();

    private static int findShortestPath(Position2 start, byte[][] map, int keysToCollect) {
        if (keysToCollect == 0) {
            return 0;
        }
        Pair<Integer, Position2> pair = new Pair<>(keysToCollect, start);
        Integer precomputed = distMap.get(pair);
        if (precomputed != null) return precomputed;

        count.incrementAndGet();
        Map<Position2, Integer> reachable = findAllReachable(start, map, keysToCollect);

        int bestSoFar = 999999999;
        for (Map.Entry<Position2, Integer> entry : reachable.entrySet()) {
            Position2 pos = entry.getKey();
            byte c = map[pos.x][pos.y];
            if ((keysToCollect & (1 << getKeyId(c))) == 0) {
                continue;
            }
            int dist = entry.getValue();

            int toCollect = keysToCollect;
            if (Character.isLowerCase(c)) {
                toCollect &= ~(1 << getKeyId(c));
            }
            dist += findShortestPath(pos, map, toCollect);

            if (dist < bestSoFar) {
                bestSoFar = dist;
            }
        }
        distMap.put(pair, bestSoFar);
        return bestSoFar;
    }

    private static Map<Pair<Integer, Position2>, Map<Position2, Integer>> reachableMap = new HashMap<>();

    private static Map<Position2, Integer> findAllReachable(Position2 start, byte[][] map, int keysToCollect) {
        Pair<Integer, Position2> pair = new Pair<>(keysToCollect, start);
        Map<Position2, Integer> precomputed = reachableMap.get(pair);
        if (precomputed != null) return precomputed;

        Map<Position2, Integer> reachable = new HashMap<>();
        Set<Position2> visited = new HashSet<>();
        Set<Position2> currSet = new HashSet<>();
        currSet.add(start);
        Set<Position2> nextSet = new HashSet<>();
        int dist = 1;
        while (!currSet.isEmpty()) {
            for (Position2 pos : currSet) {
                for (Direction dir : Direction.values()) {
                    Position2 p = dir.move(pos);
                    if (p.x < 0 || p.y < 0 || p.x >= map.length || p.y >= map[p.x].length) {
                        continue;
                    }
                    byte b = map[p.x][p.y];
                    if (b != '.' && !Character.isLetter(b)) {
                        continue;
                    }
                    if (visited.contains(p)) {
                        continue;
                    }
                    visited.add(p);
                    if (b == '.' || Character.isLowerCase(b) || (Character.isUpperCase(b) && (keysToCollect & (1 << getKeyId(b))) == 0)) {
                        nextSet.add(p);
                    }
                    if (Character.isLowerCase(b)) {
                        reachable.put(p, dist);
                    }
                }
            }
            dist++;
            currSet.clear();
            Set<Position2> t = currSet;
            currSet = nextSet;
            nextSet = t;
        }
        reachableMap.put(pair, reachable);
        return reachable;
    }

    private static int findAllKeys(byte[][] map) {
        int keysToCollect = 0;
        for (byte[] bytes : map) {
            for (byte aByte : bytes) {
                if (Character.isLowerCase(aByte)) {
                    keysToCollect |= 1 << getKeyId(aByte);
                }
            }
        }
        return keysToCollect;
    }

    private static int getKeyId(byte b) {
        return b & 31;
    }

    private static Position2 findStart(byte[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == '@') {
                    return new Position2(i, j);
                }
            }
        }
        throw new RuntimeException("WTF");
    }


    enum Direction {
        DOWN(0, 1),
        UP(0, -1),
        RIGHT(1, 0),
        LEFT(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public Position2 move(Position2 pos) {
            return pos.add(dx, dy);
        }

    }
}
