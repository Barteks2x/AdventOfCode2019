package day19;

import intcode.Intcode;
import utils.Area2;
import utils.Position2;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Main19 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in19.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);

        Area2 area = new Area2(new Position2(0, 0), new Position2(49, 49));
        Queue<Integer> coords = new ArrayDeque<>(50 * 50);
        int cnt = 0;
        int firstNonzeroY = -1;
        for (Position2 pos : area) {
            if (pos.x == 0) System.out.println();
            boolean out = hasBeam(program, coords, pos);
            cnt += out ? 1 : 0;
            System.out.print(out ? '#' : '.');
            if (firstNonzeroY < 0 && out && pos.y != 0) {
                firstNonzeroY = pos.y;
            }
        }
        System.out.println();
        System.out.println("Part1 = " + cnt);

        // I know there is a way more efficient way to do it, but this is the way I came up on first attempt
        int y = firstNonzeroY;
        while (true) {
            int x = 0;

            while (!hasBeam(program, coords, new Position2(x, y))) x++;

            int startX = x;
            int sizeX = 0;
            while (hasBeam(program, coords, new Position2(x, y))) {
                x++;
                sizeX++;
            }
            int endX = x;

            int sizeY = -1;
            for (x = startX; (endX - x) >= 100; x++) {
                sizeY = 0;
                int testY = y;
                while (hasBeam(program, coords, new Position2(x, testY))) {
                    testY++;
                    sizeY++;
                }
                if (sizeY >= 100) {
                    sizeX = endX - x;
                    break;
                }
            }

            if (sizeX >= 100 && sizeY >= 100) {
                System.out.println("Found part2 Y=" + y + ", sx=" + sizeX + ", sy=" + sizeY);
                System.out.println(x + ", " + y);
                System.out.println(x * 10000 + y);
                break;
            }
            //System.out.println("Tested Y=" + y + ", sx=" + sizeX + ", sy=" + sizeY);
            y++;
        }

    }

    static Map<Position2, Boolean> isInBeam = new HashMap<>();


    private static boolean hasBeam(BigInteger[] program, Queue<Integer> coords, Position2 pos) {
        return isInBeam.computeIfAbsent(pos, p -> {
            coords.add(p.x);
            coords.add(p.y);
            AtomicInteger out = new AtomicInteger();
            Intcode.runProgram(program, coords::poll, out::addAndGet, 1024);
            coords.clear();
            return out.get() == 1;
        });
    }


}
