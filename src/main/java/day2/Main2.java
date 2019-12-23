package day2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main2 {
    public static void main(String[] args) throws IOException {
        String s = Files.readAllLines(Paths.get("run/in2.txt")).get(0);
        AtomicInteger idx = new AtomicInteger();
        Map<Integer, Integer> opcodes = Arrays.stream(s.split(",")).map(Integer::parseInt)
                .collect(Collectors.toMap(x -> idx.getAndIncrement(), x -> x));
        System.out.println("part1 = " + runProgram(new HashMap<>(opcodes), 12, 2));

        // yes, this is slow, but it works and is fast enough
        int i = 1;
        while (true) {
            for (int a = 0; a < i; a++) {
                for (int b = 0; b < i; b++) {
                    try {
                        if (runProgram(new HashMap<>(opcodes), a, b) == 19690720) {
                            System.out.println("part2 = " + a + ", " + b + " (" + (a * 100 + b) + ")");
                            return;
                        }
                    } catch (RuntimeException ignored) {
                    }
                }
            }
            i++;
        }
    }

    private static int runProgram(Map<Integer, Integer> opcodes, int in1, int in2) {
        opcodes.put(1, in1);
        opcodes.put(2, in2);
        int pc = 0;
        while (opcodes.computeIfAbsent(pc, x -> 0) != 99) {
            int opcode = opcodes.get(pc);
            int p1 = opcodes.get(pc + 1);
            int p2 = opcodes.get(pc + 2);
            int p3 = opcodes.get(pc + 3);
            if (opcode == 1) {
                opcodes.put(p3, opcodes.computeIfAbsent(p1, x -> 0) + opcodes.computeIfAbsent(p2, x -> 0));
            } else if (opcode == 2) {
                opcodes.put(p3, opcodes.computeIfAbsent(p1, x -> 0) * opcodes.computeIfAbsent(p2, x -> 0));
            } else {
                throw new RuntimeException();
            }
            pc += 4;
        }
        return opcodes.get(0);
    }
}
