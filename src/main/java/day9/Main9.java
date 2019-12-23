package day9;

import intcode.Intcode;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

public class Main9 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in9.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);

        System.out.print("Part1 = ");
        Scanner scanner = new Scanner("1");
        Intcode.runProgram(program, scanner::nextBigInteger, System.out::println, 1024 * 1024, 0);

        System.out.print("Part2 = ");
        scanner = new Scanner("2");
        Intcode.runProgram(program, scanner::nextBigInteger, System.out::println, 1024 * 1024, 0);
    }
}
