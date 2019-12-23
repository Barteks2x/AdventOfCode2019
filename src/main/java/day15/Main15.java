package day15;

import intcode.Intcode;
import utils.Position2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class Main15 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in15.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);


        class Handler implements IntSupplier, IntConsumer {
            ArrayDeque<Direction> path = new ArrayDeque<>();
            Map<Position2, Status> areaMap = new HashMap<>();

            int maxAbsPos = 0;
            Position2 currentPos = new Position2(0, 0);
            Direction attemptedDirection;

            {
                areaMap.put(currentPos, Status.FREE);
            }

            @Override
            public void accept(int value) {
                Status status = Status.values()[value];
                Position2 moved = attemptedDirection.move(currentPos);
                maxAbsPos = Math.max(maxAbsPos, Math.max(
                        Math.abs(moved.x), Math.abs(moved.y)
                ));
                areaMap.put(moved, status);
                if (status != Status.WALL) {
                    currentPos = moved;
                } else {
                    path.pop();
                }
            }

            @Override
            public int getAsInt() {
                for (Direction dir : Direction.values()) {
                    if (dir == Direction.NONE) {
                        continue;
                    }
                    Position2 move = dir.move(currentPos);
                    if (!areaMap.containsKey(move) && Math.abs(move.x) < 512 && Math.abs(move.y) < 512) {
                        attemptedDirection = dir;
                        path.push(dir);
                        return dir.ordinal();
                    }
                }
                Direction lastDir = path.pop();
                attemptedDirection = lastDir.opposite();
                return lastDir.opposite().ordinal();
            }
        }
        Handler handler = new Handler();
        try {
            Intcode.runProgram(program, handler, handler);
        } catch (RuntimeException ignored) {
            //ignored.printStackTrace();
        }
        BufferedImage img = new BufferedImage(handler.maxAbsPos * 2 + 1, handler.maxAbsPos * 2 + 1, BufferedImage.TYPE_INT_RGB);
        Map<Position2, Status> maze = handler.areaMap;
        for (Map.Entry<Position2, Status> entry : maze.entrySet()) {
            Position2 pos = entry.getKey();
            Status status = entry.getValue();

            int color = status == Status.WALL ? 0x00FF00 : status == Status.FREE ? 0x777777 : 0xFF0000;
            if (pos.x == 0 && pos.y == 0) {
                color |= 0x000000FF;
            }
            int x = pos.x + handler.maxAbsPos;
            int y = pos.y + handler.maxAbsPos;
            img.setRGB(x, y, color);
        }

        ImageIO.write(img, "PNG", new FileOutputStream("run/out15.png"));
        int distance = findDistance(maze);
        System.out.println("Part1 = " + distance);

        int stepsToFill = 0;

        while (maze.containsValue(Status.FREE)) {
            stepsToFill++;
            Set<Position2> newlyFilled = new HashSet<>();
            for (Map.Entry<Position2, Status> e : maze.entrySet()) {
                if (e.getValue() != Status.FREE) continue;
                boolean hasOxygen = false;
                for (Direction value : Direction.values()) {
                    if (value == Direction.NONE) continue;
                    Position2 moved = value.move(e.getKey());
                    if (maze.get(moved) == Status.OXYGEN) hasOxygen = true;
                }
                if (hasOxygen) {
                    newlyFilled.add(e.getKey());
                }
            }
            newlyFilled.forEach(p -> maze.put(p, Status.OXYGEN));
        }
        System.out.println("Part2 = " + stepsToFill);
    }

    private static int findDistance(Map<Position2, Status> maze) {
        Set<Position2> currentPath = new HashSet<>();
        Position2 pos = new Position2(0, 0);
        currentPath.add(pos);
        return findDistance(maze, currentPath, pos);
    }

    private static int findDistance(Map<Position2, Status> maze, Set<Position2> currentPath, Position2 pos) {
        if (maze.get(pos) == Status.OXYGEN) {
            return 0;
        }
        int minDist = 9999999;
        for (Direction value : Direction.values()) {
            if (value == Direction.NONE) continue;
            Position2 moved = value.move(pos);
            if (currentPath.contains(moved)) continue;
            if (!maze.containsKey(moved) || maze.get(moved) == Status.WALL) continue;
            Set<Position2> p = new HashSet<>(currentPath);
            p.add(moved);
            int d = findDistance(maze, p, moved) + 1;
            if (d < minDist) {
                minDist = d;
            }
        }
        return minDist;
    }

    enum Direction {
        NONE(0, 0),
        NORTH(0, 1),
        SOUTH(0, -1),
        WEST(1, 0),
        EAST(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        Direction opposite() {
            switch (this) {
                case NONE:
                    return NONE;
                case NORTH:
                    return SOUTH;
                case SOUTH:
                    return NORTH;
                case WEST:
                    return EAST;
                case EAST:
                    return WEST;
                default:
                    throw new Error();
            }
        }

        public Position2 move(Position2 pos) {
            return pos.add(dx, dy);
        }
    }

    enum Status {
        WALL,
        FREE,
        OXYGEN
    }
}
