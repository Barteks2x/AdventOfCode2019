package day21;

import intcode.Intcode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main21 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in21.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);

        String prog0 = "NOT C J\n" +
                "AND D J\n" +
                "NOT B T\n" +
                "AND A T\n" +
                "AND D T\n" +
                "OR T J\n" +
                "WALK\n";
        String prog1 = "NOT A J\n" +
                "NOT B T\n" +
                "AND D T\n" +
                "AND C T\n" +
                "OR T J\n" +
                "NOT C T\n" +
                "AND B T\n" +
                "AND D T\n" +
                "OR T J\n" +
                "WALK\n";
        String prog2 = "NOT C J\n" +
                "AND B J\n" +
                "NOT C T\n" +
                "AND D T\n" +
                "OR T J\n" +
                "NOT B T\n" +
                "AND A T\n" +
                "AND D T\n" +
                "OR T J\n" +
                "WALK\n";
        String prog3 = "NOT C J\n" +
                "AND D J\n" +
                "NOT B T\n" +
                "AND A T\n" +
                "AND D T\n" +
                "OR T J\n" +
                "NOT A T\n" +
                "OR T J\n" +
                "WALK\n";

        ByteArrayInputStream is = new ByteArrayInputStream(
                prog3.getBytes(StandardCharsets.US_ASCII));
        Intcode.runProgram(program, () -> {
            return is.read();
        }, in -> {
            if (in > 127) {
                System.out.println(in);
            } else {
                System.out.print((char) in);
            }
        });

        String prog4 = "NOT E J\n" +
                "NOT B T\n" +
                "AND T J\n" +
                "NOT A T\n" +
                "OR T J\n" +
                "NOT B T\n" +
                "AND D T\n" +
                "OR T J\n" +
                "NOT C T\n" +
                "AND D T\n" +
                "AND H T\n" +
                "OR T J\n" +
                "RUN\n";

        String prog5 = "NOT A T\n" +
                "NOT B J\n" +
                "AND D J\n" +
                "OR T J\n" +
                "NOT C T\n" +
                "AND D T\n" +
                "AND H T\n" +
                "OR T J\n" +
                "RUN\n";
        ByteArrayInputStream is2 = new ByteArrayInputStream(
                prog5.getBytes(StandardCharsets.US_ASCII));
        Intcode.runProgram(program, () -> {
            return is2.read();
        }, in -> {
            if (in > 127) {
                System.out.println(in);
            } else {
                System.out.print((char) in);
            }
        });

        List<String> allInsns = new ArrayList<>();
        for (String r1 : new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "T", "J"}) {
            allInsns.add("NOT " + r1);
            for (String r2 : new String[]{"T", "J"}) {
                allInsns.add("AND " + r1 + " " + r2);
                allInsns.add("OR " + r1 + " " + r2);
            }
        }
    }

}
