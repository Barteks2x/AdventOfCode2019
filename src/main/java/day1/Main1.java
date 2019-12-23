package day1;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main1 {
    public static void main(String[] args) throws Exception {
        // part1
        System.out.println("part1 = " + Files.readAllLines(Paths.get("run/in1.txt")).stream()
                .mapToInt(Integer::parseInt)
                .map(x -> x / 3 - 2).sum());
        // part2

        System.out.println("part2 = " + Files.readAllLines(Paths.get("run/in1.txt")).stream()
                .mapToInt(Integer::parseInt)
                .map(Main1::moduleFuel).sum());

    }

    static int moduleFuel(int mass) {
        int fuel = 0;
        int extraFuel = mass;
        do {
            extraFuel = Math.max(0, extraFuel / 3 - 2);
            fuel += extraFuel;
        } while (extraFuel > 0);
        return fuel;
    }
}
