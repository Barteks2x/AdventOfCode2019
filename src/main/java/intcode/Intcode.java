package intcode;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static utils.Utils.catching;

public class Intcode {

    public static Thread makeThread(int[] program, LinkedBlockingDeque<Integer> inputs, LinkedBlockingDeque<Integer> outputs) {
        Thread t = new Thread(() -> {
            runProgram(program,
                    catching(() -> (int)  inputs.poll(10000, TimeUnit.SECONDS)),
                    catching(x -> outputs.offer(x, 100000, TimeUnit.SECONDS)));
        });
        return t;
    }

    public static Thread makeThread(BigInteger[] program, Supplier<BigInteger> inputs, Consumer<BigInteger> outputs, int mx) {
        return new Thread(() -> {
            try {
                runProgram(program, inputs, outputs, mx);
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw t;
                }
                t.printStackTrace();
                throw t;
            }
        });
    }

    public static List<Integer> runProgram(int[] program, int... inputs) {
        List<Integer> outs = new ArrayList<>();
        Queue<Integer> ins = new ArrayDeque<>(inputs.length);
        for (int in : inputs) {
            ins.add(in);
        }
        runProgram(program, ins::poll, outs::add);
        return outs;
    }

    public static void runProgram(int[] program, IntSupplier in, IntConsumer out) {
        runProgram(program, in, out, 1024 * 1024, 0);
    }

    public static void runProgram(int[] program, IntSupplier in, IntConsumer out, EnumSet<VMModifier> vmMods) {
        runProgram(program, in, out, 1024 * 1024, 0, vmMods);
    }

    public static void runProgram(int[] program, IntSupplier in, IntConsumer out, int mx, int pc) {
        runProgram(program, in, out, mx, pc, EnumSet.noneOf(VMModifier.class));
    }

    public static void runProgram(int[] program, IntSupplier in, IntConsumer out, int mx, int pc, EnumSet<VMModifier> vmMods) {
        BigInteger[] memory = new BigInteger[mx];
        Arrays.fill(memory, BigInteger.ZERO);
        for (int i = 0; i < program.length; i++) {
            memory[i] = BigInteger.valueOf(program[i]);
        }
        Supplier<BigInteger> bigIn = () -> BigInteger.valueOf(in.getAsInt());
        Consumer<BigInteger> bigOut = x -> out.accept(x.intValueExact());
        runProgram(memory, bigIn, bigOut, mx, pc, vmMods);
    }

    public static BigInteger[] runProgram(BigInteger[] program, IntSupplier in, IntConsumer out, EnumSet<VMModifier> vmMods) {
        return runProgram(program, () -> BigInteger.valueOf(in.getAsInt()), x -> out.accept(x.intValueExact()), 1024 * 1024, 0, vmMods);
    }

    public static void runProgram(BigInteger[] program, IntSupplier in, IntConsumer out) {
        runProgram(program, in, out, 1024 * 1024);
    }

    public static void runProgram(BigInteger[] program, Supplier<BigInteger> in, Consumer<BigInteger> out, int mx) {
        runProgram(program, in, out, mx, 0);
    }

    public static void runProgram(BigInteger[] program, IntSupplier in, IntConsumer out, int mx) {
        runProgram(program, () -> BigInteger.valueOf(in.getAsInt()), x -> out.accept(x.intValueExact()), mx, 0);
    }

    public static void runProgram(BigInteger[] program, Supplier<BigInteger> in, Consumer<BigInteger> out, int mx, int pc) {
        runProgram(program, in, out, mx, pc, EnumSet.noneOf(VMModifier.class));
    }

    public static BigInteger[] runProgram(BigInteger[] program, Supplier<BigInteger> in, Consumer<BigInteger> out, int mx, int pc, EnumSet<VMModifier> vmMods) {
        BigInteger[] memory;
        if (!vmMods.contains(VMModifier.NO_MEMCLONE) || program.length < mx) {
            if (program.length >= mx) {
                memory = program.clone();
            } else {
                memory = new BigInteger[mx];
                Arrays.fill(memory, BigInteger.ZERO);
                System.arraycopy(program, 0, memory, 0, program.length);
            }
        } else {
            memory = program;
        }
        BigInteger rel = BigInteger.ZERO;
        while (true) {
            int opcode = memory[pc].intValueExact();
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
            BigInteger p1o = p1m == 2 ? rel.add(memory[pc + 1]) : memory[pc + 1];
            BigInteger p2o = p2m == 2 ? rel.add(memory[pc + 2]) : memory[pc + 2];
            BigInteger p3o = p3m == 2 ? rel.add(memory[pc + 3]) : memory[pc + 3];
            // input params, respecting param mode
            Supplier<BigInteger> p1 = () -> p1m == 0 || p1m == 2 ? memory[p1o.intValueExact()] : p1o;
            Supplier<BigInteger> p2 = () -> p2m == 0 || p2m == 2 ? memory[p2o.intValueExact()] : p2o;
            Supplier<BigInteger> p3 = () -> p3m == 0 || p3m == 2 ? memory[p3o.intValueExact()] : p3o;

            pc++;
            switch (insn) {
                case 1:
                    memory[p3o.intValueExact()] = p1.get().add(p2.get());
                    pc += 3;
                    break;
                case 2:
                    memory[p3o.intValueExact()] = p1.get().multiply(p2.get());
                    pc += 3;
                    break;
                case 3:
                    memory[p1o.intValueExact()] = in.get();
                    pc += 1;
                    break;
                case 4:
                    out.accept(p1.get());
                    pc += 1;
                    break;
                case 5:
                    if (!p1.get().equals(BigInteger.ZERO)) {
                        pc = p2.get().intValueExact();
                    } else {
                        pc += 2;
                    }
                    break;
                case 6:
                    if (p1.get().equals(BigInteger.ZERO)) {
                        pc = p2.get().intValueExact();
                    } else {
                        pc += 2;
                    }
                    break;
                case 7:
                    memory[p3o.intValueExact()] = p1.get().compareTo(p2.get()) < 0 ? BigInteger.ONE : BigInteger.ZERO;
                    pc += 3;
                    break;
                case 8:
                    memory[p3o.intValueExact()] = p1.get().equals(p2.get()) ? BigInteger.ONE : BigInteger.ZERO;
                    pc += 3;
                    break;
                case 9:
                    rel = rel.add(p1.get());
                    pc += 1;
                    break;
                default:
                    throw new RuntimeException("Illegal opcode " + opcode + " at " + pc);
            }
        }
        return memory;
    }

    public enum VMModifier {
        NO_MEMCLONE
    }
}
