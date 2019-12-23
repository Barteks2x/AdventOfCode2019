package day17;

import intcode.Intcode;
import utils.Pair;
import utils.Position2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Main17 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in17.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);

        CameraView cam = new CameraView();

        Intcode.runProgram(program, () -> {
            throw new RuntimeException();
        }, in -> {
            System.out.print((char) in);
            cam.append((char) in);
        });

        AtomicInteger total = new AtomicInteger();
        cam.forEachIntersection(pos -> total.addAndGet(pos.x * pos.y));

        List<Pair<Integer, Character>> lengths = new ArrayList<>();
        cam.getScaffoldLengths(lengths);
        System.out.println(lengths);
        System.out.println(lengths.size());


        StringBuilder sb = new StringBuilder(80).append("R,"); // for my input, robot has to turn right first
        for (Pair<Integer, Character> pair : lengths) {
            sb.append(pair.a).append(',');
            if (pair.b != '.')
                sb.append(pair.b).append(',');
        }
        System.out.println(sb);

        String s = sb.toString();

        String aout = null, bout = null, cout = null, xout = null;
        search:
        for (int aStart = 0; aStart < s.length(); aStart++) {
            System.out.println("AStart=" + aStart);
            for (int aEnd = Math.min(s.length(), aStart + 21); aEnd >= aStart + 2; aEnd--) {
                System.out.println("AEnd=" + aEnd);
                String a = s.substring(aStart, aEnd);
                if (a.startsWith(",") || !a.endsWith(",")) continue;
                if (a.length() > 21) continue;

                String xal = s.replaceAll(a.replace('.', 'L'), "A,");
                for (int bStart = aEnd; bStart < s.length(); bStart++) {
                    for (int bEnd = bStart + 2; bEnd <= s.length(); bEnd++) {
                        String b = s.substring(bStart, bEnd);
                        if (b.startsWith(",") || !b.endsWith(",")) continue;
                        if (b.length() > 21) continue;

                        String xabl = xal.replaceAll(b.replace('.', 'L'), "B,");

                        String xbl = s.replaceAll(b.replace('.', 'L'), "B,");
                        String xbal = xbl.replaceAll(a.replace('.', 'L'), "A,");
                        for (int cStart = bEnd; cStart < s.length(); cStart++) {
                            for (int cEnd = cStart + 2; cEnd <= s.length(); cEnd++) {
                                String c = s.substring(cStart, cEnd);
                                if (c.startsWith(",") || !c.endsWith(",")) continue;
                                if (c.length() > 21) continue;

                                String xabcl = xabl.replaceAll(c.replace('.', 'L'), "C,");

                                String xbacl = xbal.replaceAll(c.replace('.', 'L'), "C,");

                                String xacbl = xal.replaceAll(c.replace('.', 'L'), "C,");

                                String xbcal = xbl.replaceAll(c.replace('.', 'L'), "C,");

                                String xcl = s.replaceAll(c.replace('.', 'L'), "C,");

                                String xcabl = xcl.replaceAll(a.replace('.', 'L'), "A,")
                                        .replaceAll(b.replace('.', 'L'), "B,");

                                String xcbal = xcl.replaceAll(b.replace('.', 'L'), "B,")
                                        .replaceAll(a.replace('.', 'L'), "A,");
                                for (String x : new String[]{xabcl, xbacl, xacbl, xbcal, xcabl, xcbal}) {
                                    if (x.matches("([ABC],)+")) {
                                        System.out.println(a);
                                        System.out.println(b);
                                        System.out.println(c);
                                        System.out.println(x);
                                        aout = a;
                                        bout = b;
                                        cout = c;
                                        xout = x;
                                        break search;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        assert xout != null;
        String endStr = xout.substring(0, xout.length() - 1) + '\n'
                + aout.substring(0, aout.length() - 1) + '\n'
                + bout.substring(0, bout.length() - 1) + '\n'
                + cout.substring(0, cout.length() - 1) + "\ny\n";

        byte[] bytes = endStr.getBytes(StandardCharsets.US_ASCII);
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

        program[0] = new BigInteger("2");
        Intcode.runProgram(program, stream::read, in -> {
            if (in > 127) {
                System.out.println("Part1 = " + total);
                System.out.println("Part2 = " + in);
            } else {
                System.out.print((char) in);
            }
        });


    }

    private static class CameraView {
        private final Map<Position2, Character> view = new HashMap<>();

        int x, y;

        public void append(char c) {
            if (c == '\n') {
                y++;
                x = 0;
                return;
            }
            view.put(new Position2(x, y), c);
            x++;
        }

        public void forEachIntersection(Consumer<Position2> consumer) {
            view.forEach((pos, c) -> {
                if (c == '#' && countNeighbors(view, pos) == 4) {
                    consumer.accept(pos);
                }
            });
        }

        public void getScaffoldLengths(List<Pair<Integer, Character>> lengths) {
            HashMap<Position2, Character> view = new HashMap<>(this.view);
            Position2 currPos = null;
            for (Position2 pos : view.keySet()) {
                if (view.get(pos) == '^' || view.get(pos) == 'v' || view.get(pos) == '>' || view.get(pos) == '<') {
                    currPos = pos;
                    break;
                }
            }
            view.put(currPos, '.');
            assert currPos != null;
            Direction d;
            while (true) {
                d = null;
                for (Direction dir : Direction.values()) {
                    if (view.get(dir.move(currPos)) != null && view.get(dir.move(currPos)) == '#') {
                        d = dir;
                        break;
                    }
                }
                if (d == null) {
                    break;
                }
                currPos = d.move(currPos);
                int len = 0;
                while (view.get(currPos) != null && view.get(currPos) == '#') {
                    if (countNeighbors(view, currPos) < 3) {
                        view.put(currPos, '.');
                    }
                    currPos = d.move(currPos);
                    len++;
                }
                currPos = d.opposite().move(currPos);

                Direction d2 = null;
                for (Direction dir : Direction.values()) {
                    if (view.get(dir.move(currPos)) != null && view.get(dir.move(currPos)) == '#') {
                        d2 = dir;
                        break;
                    }
                }
                char turn = '.';
                if (d2 != null) {
                    turn = d2.turningDirFrom(d);
                }
                lengths.add(new Pair<>(len, turn));
            }
        }

        private int countNeighbors(Map<Position2, Character> view, Position2 pos) {
            Character l = view.get(pos.add(-1, 0));
            Character r = view.get(pos.add(1, 0));
            Character u = view.get(pos.add(0, -1));
            Character d = view.get(pos.add(0, 1));
            int i = 0;
            if (l != null && l == '#') i++;
            if (r != null && r == '#') i++;
            if (u != null && u == '#') i++;
            if (d != null && d == '#') i++;
            return i;
        }
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

        Direction opposite() {
            switch (this) {
                case DOWN:
                    return UP;
                case UP:
                    return DOWN;
                case RIGHT:
                    return LEFT;
                case LEFT:
                    return RIGHT;
                default:
                    throw new Error();
            }
        }

        public Position2 move(Position2 pos) {
            return pos.add(dx, dy);
        }

        public char turningDirFrom(Direction prev) {
            if (prev == UP) {
                if (this == LEFT) return 'L';
                if (this == RIGHT) return 'R';
            }

            if (prev == DOWN) {
                if (this == LEFT) return 'R';
                if (this == RIGHT) return 'L';
            }

            if (prev == RIGHT) {
                if (this == DOWN) return 'R';
                if (this == UP) return 'L';
            }

            if (prev == LEFT) {
                if (this == DOWN) return 'L';
                if (this == UP) return 'R';
            }

            throw new IllegalArgumentException();
        }
    }

}
