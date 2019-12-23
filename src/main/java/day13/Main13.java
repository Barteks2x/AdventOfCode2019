package day13;

import intcode.Intcode;
import utils.Position2;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.EventQueue;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main13 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in13.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);

        Map<Position2, Integer> screen = new HashMap<>();

        AtomicInteger x = new AtomicInteger();
        AtomicInteger y = new AtomicInteger();

        AtomicInteger step = new AtomicInteger();

        Intcode.runProgram(program, () -> {
            throw new RuntimeException();
        }, out -> {
            int s = step.getAndIncrement() % 3;
            if (s == 0) {
                x.set(out);
            } else if (s == 1) {
                y.set(out);
            } else {
                screen.put(new Position2(x.get(), y.get()), out);
            }
        });

        long blockCount = screen.values().stream().mapToInt(a -> a).filter(a -> a == 2).count();
        System.out.println("Part1 = " + blockCount);

        program[0] = new BigInteger("2");

        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("GameAOC");
            JPanel gamePanel = new GamePanel(program);
            frame.setContentPane(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            frame.setVisible(true);
        });
    }
}
