package day8;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main8 {
    public static void main(String[] args) throws Throwable {
        int sizeX = 25;
        int sizeY = 6;
        int size = sizeX * sizeY;
        String data = Files.readAllLines(Paths.get("run/in8.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .reduce((x, y) -> x + y).orElse("");
        char[] chars = data.toCharArray();
        int mc0 = 10000000, mc1 = 0, mc2 = 0;
        int c0 = 0, c1 = 0, c2 = 0;
        int j = 0;
        for (char c : chars) {
            if (c == '0') {
                c0++;
            } else if (c == '1') {
                c1++;
            } else if (c == '2') {
                c2++;
            }
            j++;
            if (j == size) {
                if (c0 < mc0) {
                    mc0 = c0;
                    mc1 = c1;
                    mc2 = c2;
                }
                j = 0;
                c0 = 0;
                c1 = 0;
                c2 = 0;
            }
        }
        System.out.println("Part1 = " + mc1 * mc2);

        BufferedImage img = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_ARGB);
        boolean[][] written = new boolean[sizeX][sizeY];
        int x = 0;
        int y = 0;
        for (char c : chars) {
            int color;
            if (c == '0') {
                color = 0xFF000000;
            } else if (c == '1') {
                color = 0xFFFFFFFF;
            } else if (c == '2') {
                color = 0;
            } else {
                throw new RuntimeException();
            }
            if (!written[x][y] && color != 0) {
                img.setRGB(x, y, color);
                written[x][y] = true;
            }

            x++;
            if (x == sizeX) {
                x = 0;
                y++;
                if (y == sizeY) {
                    y = 0;
                }
            }
        }

        ImageIO.write(img, "PNG", Files.newOutputStream(Paths.get("run/Out8_Part2.png")));
        System.out.println("Part2 = run/Out8_Part2.png");
    }
}
