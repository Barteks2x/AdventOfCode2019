package day5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main5 {
    public static void main(String[] args) throws IOException {
        int[] program = Files.readAllLines(Paths.get("run/in5.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .mapToInt(Integer::parseInt)
                .toArray();

        int memory = 1024 * 1024 * 64;

        List<Integer> outputs = runProgram(program, new Scanner("1"), memory);
        System.out.println("Part1 = " + outputs.get(outputs.size() - 1));

        outputs = runProgram(program, new Scanner("5"), memory);
        System.out.println("Part2 = " + outputs.get(outputs.size() - 1));
    }


    private static List<Integer> runProgram(int[] program, Scanner inputs, int mx) {
        int[] memory = new int[mx];
        System.arraycopy(program, 0, memory, 0, program.length);
        int pc = 0;
        List<Integer> outputs = new ArrayList<>();
        while (true) {
            int opcode = memory[pc];
            int insn = opcode % 100;
            if (insn == 99) {
                break;
            }
            int paramMode = opcode / 100;
            // param modes for each parameter
            int p1m = paramMode % 10;
            paramMode /= 10;
            int p2m = paramMode % 10;
            paramMode /= 10;
            int p3m = paramMode % 10;

            // output memory location params
            int p1o = memory[pc + 1];
            int p2o = memory[pc + 2];
            int p3o = memory[pc + 3];
            // input params, respecting param mode
            int p1 = p1m == 0 ? memory[p1o] : p1o;
            int p2 = p2m == 0 ? memory[p2o] : p2o;
            int p3 = p3m == 0 ? memory[p3o] : p3o;

            pc++;
            switch (insn) {
                case 1:
                    memory[p3o] = p1 + p2;
                    pc += 3;
                    break;
                case 2:
                    memory[p3o] = p1 * p2;
                    pc += 3;
                    break;
                case 3:
                    memory[p1o] = inputs.nextInt();
                    pc += 1;
                    break;
                case 4:
                    outputs.add(p1);
                    pc += 1;
                    break;
                case 5:
                    if (p1 != 0) {
                        pc = p2;
                    } else {
                        pc += 2;
                    }
                    break;
                case 6:
                    if (p1 == 0) {
                        pc = p2;
                    } else {
                        pc += 2;
                    }
                    break;
                case 7:
                    memory[p3o] = p1 < p2 ? 1 : 0;
                    pc += 3;
                    break;
                case 8:
                    memory[p3o] = p1 == p2 ? 1 : 0;
                    pc += 3;
                    break;
                default:
                    throw new RuntimeException("Illegal opcode " + opcode);
            }

        }
        return outputs;
    }
}
