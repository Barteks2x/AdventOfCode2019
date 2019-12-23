package day7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static utils.Utils.catching;

public class Main7 {
    public static void main(String[] args) throws IOException {
        int[] program = Files.readAllLines(Paths.get("run/in7.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .mapToInt(Integer::parseInt)
                .toArray();

        int memory = 2048;

        part1(program, memory);
        part2(program, memory);
    }

    private static void part1(int[] program, int memory) {
        int maxo = 0;
        int m1 = 0, m2 = 0, m3 = 0, m4 = 0, m5 = 0;
        for (int i = 0; i <= 4; i++) {
            for (int j = 0; j <= 4; j++) {
                if (i == j) continue;
                for (int k = 0; k <= 4; k++) {
                    if (k == i || k == j) continue;
                    for (int l = 0; l <= 4; l++) {
                        if (l == i || l == j || l == k) continue;
                        for (int m = 0; m <= 4; m++) {
                            if (m == i || m == j || m == k || m == l) continue;
                            Scanner in1 = new Scanner(i + " " + 0);
                            List<Integer> o1 = runProgram(program, in1, memory);

                            Scanner in2 = new Scanner(j + " " + o1.get(0));
                            List<Integer> o2 = runProgram(program, in2, memory);

                            Scanner in3 = new Scanner(k + " " + o2.get(0));
                            List<Integer> o3 = runProgram(program, in3, memory);

                            Scanner in4 = new Scanner(l + " " + o3.get(0));
                            List<Integer> o4 = runProgram(program, in4, memory);

                            Scanner in5 = new Scanner(m + " " + o4.get(0));
                            List<Integer> o5 = runProgram(program, in5, memory);
                            if (o5.get(0) > maxo) {
                                maxo = o5.get(0);
                                m1 = i;
                                m2 = j;
                                m3 = k;
                                m4 = l;
                                m5 = m;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Part1 = " + maxo);
        //System.out.print(m1);
        //System.out.print(m2);
        //System.out.print(m3);
        //System.out.print(m4);
        //System.out.print(m5);
        //System.out.println();
    }


    private static void part2(int[] program, int memory) {
        int maxo = 0;
        int m1 = 0, m2 = 0, m3 = 0, m4 = 0, m5 = 0;
        for (int i = 5; i <= 9; i++) {
            for (int j = 5; j <= 9; j++) {
                if (i == j) continue;
                for (int k = 5; k <= 9; k++) {
                    if (k == i || k == j) continue;
                    for (int l = 5; l <= 9; l++) {
                        if (l == i || l == j || l == k) continue;
                        for (int m = 5; m <= 9; m++) {
                            if (m == i || m == j || m == k || m == l) continue;
                            LinkedBlockingDeque<Integer> t1out = new LinkedBlockingDeque<>(128);
                            LinkedBlockingDeque<Integer> t2out = new LinkedBlockingDeque<>(128);
                            LinkedBlockingDeque<Integer> t3out = new LinkedBlockingDeque<>(128);
                            LinkedBlockingDeque<Integer> t4out = new LinkedBlockingDeque<>(128);
                            LinkedBlockingDeque<Integer> t5out = new LinkedBlockingDeque<>(128);
                            t5out.add(i);
                            t5out.add(0);
                            t1out.add(j);
                            t2out.add(k);
                            t3out.add(l);
                            t4out.add(m);
                            Thread t1 = new Thread(() -> {
                                runProgram(program,
                                        catching(() -> (int) t5out.poll(10000, TimeUnit.SECONDS)),
                                        catching(x -> t1out.offer(x, 100000, TimeUnit.SECONDS)),
                                        memory);
                            });
                            Thread t2 = new Thread(() -> {
                                runProgram(program,
                                        catching(() -> (int) t1out.poll(10000, TimeUnit.SECONDS)),
                                        catching(x -> t2out.offer(x, 100000, TimeUnit.SECONDS)),
                                        memory);
                            });
                            Thread t3 = new Thread(() -> {
                                runProgram(program,
                                        catching(() -> (int) t2out.poll(10000, TimeUnit.SECONDS)),
                                        catching(x -> t3out.offer(x, 100000, TimeUnit.SECONDS)),
                                        memory);
                            });
                            Thread t4 = new Thread(() -> {
                                runProgram(program,
                                        catching(() -> (int) t3out.poll(10000, TimeUnit.SECONDS)),
                                        catching(x -> t4out.offer(x, 100000, TimeUnit.SECONDS)),
                                        memory);
                            });
                            Thread t5 = new Thread(() -> {
                                runProgram(program,
                                        catching(() -> (int) t4out.poll(10000, TimeUnit.SECONDS)),
                                        catching(x -> t5out.offer(x, 100000, TimeUnit.SECONDS)),
                                        memory);
                            });
                            t1.start();
                            t2.start();
                            t3.start();
                            t4.start();
                            t5.start();

                            try {
                                t1.join();
                                t2.join();
                                t3.join();
                                t4.join();
                                t5.join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            int out = t5out.poll();
                            if (out > maxo) {
                                //System.out.println("max=" + out);
                                maxo = out;
                                m1 = i;
                                m2 = j;
                                m3 = k;
                                m4 = l;
                                m5 = m;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Part2 = " + maxo);
        //System.out.print(m1);
        //System.out.print(m2);
        //System.out.print(m3);
        //System.out.print(m4);
        //System.out.print(m5);
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
                    throw new RuntimeException("Illegal opcode " + opcode + " at " + pc);
            }

        }
        return outputs;
    }


    private static void runProgram(int[] program, IntSupplier in, IntConsumer out, int mx) {
        int[] memory = new int[mx];
        System.arraycopy(program, 0, memory, 0, program.length);
        int pc = 0;
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
                    memory[p1o] = in.getAsInt();
                    pc += 1;
                    break;
                case 4:
                    out.accept(p1);
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
                    throw new RuntimeException("Illegal opcode " + insn + " with param modes " + (opcode / 100) + " at " + pc);
            }

        }
    }
}
