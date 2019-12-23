package day3;

import utils.Position2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class Main3 {

    public static void main(String[] args) throws IOException {
        List<String[]> list = Files.readAllLines(Paths.get("run/in3.txt")).stream()
                .filter(s -> !s.startsWith("#") && !s.isEmpty())
                .map(s -> s.split(",")).collect(Collectors.toList());

        part1(list);
        part2(list);
    }

    private static void part1(List<String[]> list) {
        Map<Position2, Set<Integer>> takenPos = new HashMap<>();

        int closestD = Integer.MAX_VALUE;
        takenPos.put(new Position2(0, 0), new HashSet<>());
        for (int i1 = 0; i1 < list.size(); i1++) {
            String[] dirs = list.get(i1);
            int x = 0, y = 0;
            for (String dir : dirs) {
                int dist = Integer.parseInt(dir.substring(1));
                char c = dir.charAt(0);
                int dx = 0, dy = 0;
                if (c == 'D') {
                    dy -= 1;
                } else if (c == 'U') {
                    dy += 1;
                } else if (c == 'L') {
                    dx -= 1;
                } else if (c == 'R') {
                    dx += 1;
                } else {
                    throw new Error("Invalid direction " + c);
                }
                for (int i = 0; i < dist; i++) {
                    Position2 pos = new Position2(x, y);
                    if (i != 0) {
                        Set<Integer> set = takenPos.computeIfAbsent(pos, a -> new HashSet<>());
                        if (!set.contains(i1) && !set.isEmpty()) {
                            int d = abs(x) + abs(y);
                            if (d < closestD && d != 0) {
                                closestD = d;
                            }
                        } else {
                            set.add(i1);
                        }
                    }
                    x += dx;
                    y += dy;
                }

            }
        }
        System.out.println("Part1 = " + closestD);
    }


    private static void part2(List<String[]> list) {
        Map<Position2, Map<Integer, Integer>> takenPos = new HashMap<>();

        int closestD = Integer.MAX_VALUE;
        takenPos.put(new Position2(0, 0), new HashMap<>());
        for (int i1 = 0; i1 < list.size(); i1++) {
            String[] dirs = list.get(i1);
            int x = 0, y = 0;
            int steps = 0;
            for (String dir : dirs) {
                int dist = Integer.parseInt(dir.substring(1));
                char c = dir.charAt(0);
                int dx = 0, dy = 0;
                if (c == 'D') {
                    dy -= 1;
                } else if (c == 'U') {
                    dy += 1;
                } else if (c == 'L') {
                    dx -= 1;
                } else if (c == 'R') {
                    dx += 1;
                } else {
                    throw new Error("WTF " + c);
                }

                for (int i = 0; i < dist; i++) {
                    Position2 pos = new Position2(x, y);

                    if (i != 0) {
                        Map<Integer, Integer> set = takenPos.computeIfAbsent(pos, a -> new HashMap<>());
                        if (!set.containsKey(i1) && !set.isEmpty()) {
                            for (Map.Entry<Integer, Integer> idDist : set.entrySet()) {
                                if (idDist.getKey() == i1) {
                                    continue;
                                }
                                int d = steps + idDist.getValue();
                                if (d < closestD && d != 0) {
                                    closestD = d;
                                }
                            }
                        }
                        if (!set.containsKey(i1)) {
                            set.put(i1, steps);
                        }
                    }
                    steps++;
                    x += dx;
                    y += dy;
                }

            }
        }
        System.out.println("Part2 = " + closestD);
    }

}
