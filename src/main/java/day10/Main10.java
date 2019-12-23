package day10;

import utils.Ratio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Main10 {
    public static void main(String[] args) throws IOException {
        char[][] asteroids = Files.readAllLines(Paths.get("run/in10.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("//"))
                .map(String::toCharArray)
                .toArray(char[][]::new);
        int[][] canSee = new int[asteroids.length][asteroids[0].length];
        for (int i = 0; i < asteroids.length; i++) {
            for (int j = 0; j < asteroids[i].length; j++) {
                if (asteroids[i][j] == '#')
                    canSee[i][j] = countVisible(asteroids, i, j);
            }
        }

        int max = 0, maxi = 0, maxj = 0;
        for (int i = 0; i < asteroids.length; i++) {
            for (int j = 0; j < asteroids[i].length; j++) {
                if (canSee[i][j] > max) {
                    max = canSee[i][j];
                    maxi = i;
                    maxj = j;
                }
            }
        }
        System.out.println("Part1 = " + max);
        //System.out.println(maxj + ", " + maxi);

        int pos = calculateVaporization(asteroids, maxi, maxj);
        System.out.println("Part2 = " + pos);
    }

    private static int calculateVaporization(char[][] asteroids, int i, int j) {
        List<Ratio> list = new ArrayList<>();
        for (int k = 0; k < asteroids.length; k++) {
            for (int l = 0; l < asteroids[k].length; l++) {
                list.add(new Ratio(k - i, l - j));
            }
        }
        list.sort(Comparator.<Ratio>comparingDouble(r -> {
            if (r.rawNum == 0 && r.rawDen == 0) {
                return -9999.;
            }
            double v = Math.atan2(r.rawNum, r.rawDen) + Math.PI / 2;
            v = (v + 2 * Math.PI) % (2 * Math.PI);
            return v;
        }).thenComparingInt(r -> r.rawDen * r.rawDen + r.rawNum * r.rawNum));

        List<List<Ratio>> angles = new ArrayList<>();
        List<Ratio> currList = null;
        Ratio prev = new Ratio(0, 0);
        for (Ratio ratio : list) {
            if (ratio.equals(new Ratio(0, 0))) {
                continue;
            }
            if (!ratio.equals(prev) || currList == null) {
                if (currList != null) {
                    angles.add(currList);
                }
                currList = new ArrayList<>();
            }
            prev = ratio;
            currList.add(ratio);
        }
        //System.out.println(list);

        int shotCount = 0;
        while (!angles.isEmpty()) {
            List<Ratio> angle = null;
            for (Iterator<List<Ratio>> iterator = angles.iterator(); iterator.hasNext(); ) {
                if (angle == null) {
                    angle = iterator.next();
                }
                boolean hit = false;
                Ratio next = angle.remove(0);
                if (asteroids[next.rawNum + i][next.rawDen + j] == '#') {
                    asteroids[next.rawNum + i][next.rawDen + j] = 'S';
                    hit = true;
                    shotCount++;
                    //System.out.println((shotCount + ": " + (next.rawDen + j) + ", " + (next.rawNum + i)));
                    if (shotCount == 200) {
                        return (next.rawDen + j) * 100 + next.rawNum + i;
                    }
                }
                if (angle.isEmpty()) {
                    iterator.remove();
                    angle = null;
                } else if (hit) {
                    angle = null;
                }

            }
        }
        return 0;
    }

    private static int countVisible(char[][] asteroids, int i, int j) {
        Set<Ratio> values = new HashSet<>();
        int count = 0;

        for (int i1 = 0; i1 < asteroids.length; i1++) {
            for (int j1 = 0; j1 < asteroids[i].length; j1++) {
                Ratio r = new Ratio(i1 - i, j1 - j);
                if ((i1 != i || j1 != j) && !values.contains(r) && asteroids[i1][j1] == '#') {
                    values.add(r);
                    count++;
                }
            }
        }
        return count;
    }
}
