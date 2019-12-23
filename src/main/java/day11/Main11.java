package day11;

import intcode.Intcode;
import utils.Position2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Main11 {
    public static void main(String[] args) throws IOException {
        BigInteger[] program = Files.readAllLines(Paths.get("run/in11.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .flatMap(x -> Arrays.stream(x.split("[,\n\r]+")))
                .map(BigInteger::new)
                .toArray(BigInteger[]::new);


        Map<Position2, Integer> colors = new HashMap<>();
        computeColors(program, colors);
        System.out.println("Part1 = " + colors.size());

        colors = new HashMap<>();
        colors.put(new Position2(0, 0), 1);
        computeColors(program, colors);

        BufferedImage img = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);

        colors.forEach((p, c) -> {
            int imgx = p.x + 512;
            int imgy = -p.y + 512;
            int color = c == 0 ? 0 : 0xFFFFFFFF;
            img.setRGB(imgx, imgy, color);
        });
        ImageIO.write(img, "PNG", new File("run/Out11_Part2.png"));
        System.out.println("Part2 = run/Out11_Part2.png");
    }

    private static void computeColors(BigInteger[] program, Map<Position2, Integer> colors) {
        // +y-up, +x->right
        AtomicInteger x = new AtomicInteger(), y = new AtomicInteger();
        AtomicInteger dx = new AtomicInteger(), dy = new AtomicInteger(1);
        AtomicInteger mode = new AtomicInteger();

        Intcode.runProgram(program, () -> colors.getOrDefault(new Position2(x.get(), y.get()), 0), insn -> {
            if (mode.getAndIncrement() == 0) {
                colors.put(new Position2(x.get(), y.get()), insn);
            } else {
                if (insn == 0) {
                    int ndx = -dy.get();
                    int ndy = dx.get();
                    dx.set(ndx);
                    dy.set(ndy);
                } else {
                    int ndx = dy.get();
                    int ndy = -dx.get();
                    dx.set(ndx);
                    dy.set(ndy);
                }
                x.set(x.get() + dx.get());
                y.set(y.get() + dy.get());
                mode.set(0);
            }
        });
    }
}
