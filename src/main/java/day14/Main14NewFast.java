package day14;

import utils.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Final version with some attempts at optimization
 */
public class Main14NewFast {
    private static final int FUEL = 1;
    private static final int ORE = 0;

    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        HashMap<String, Integer> idMap = new HashMap<>();
        idMap.put("ORE", ORE);
        idMap.put("FUEL", FUEL);
        AtomicInteger id = new AtomicInteger(Math.max(FUEL, ORE) + 1);

        List<String> list = Files.readAllLines(Paths.get("run/in14.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#")).collect(Collectors.toList());
        list.stream().map(x -> x.split("=>")[1].trim().split("\\s")[1])
                .filter(x -> !x.equals("FUEL")).forEach(x -> idMap.put(x, id.getAndIncrement()));
        final int idCount = id.get();

        long[][] reactions = new long[idCount][idCount];
        list.stream().map(x -> x.split("=>"))
                .map(x1 -> makeReaction(idMap, x1, idCount))
                .forEach(r -> reactions[r.a] = r.b);
        reactions[ORE][ORE] = -1;
        double[] reactOutInv = new double[idCount];
        for (int i = 0; i < idCount; i++) {
            reactOutInv[i] = 1.0 / reactions[i][i];
        }

        long[] targetCounts = new long[idCount];
        targetCounts[FUEL] = -1;
        doReactions(targetCounts, reactions, reactOutInv);//13312
        System.out.println("Req ore: " + -targetCounts[ORE]);
        int fuelMade = doReactions2(reactions, 1000000000000L, reactOutInv); //82892754
        System.out.println("Fuel made: " + fuelMade);
        System.out.println((System.currentTimeMillis() - time) * 0.001);
    }

    private static Pair<Integer, long[]> makeReaction(HashMap<String, Integer> idMap, String[] x, int idCount) {
        long[] ret = new long[idCount];
        String[] out = x[1].trim().split("\\s");
        int outType = idMap.get(out[1]);
        ret[outType] = Integer.parseInt(out[0]);

        for (String s : x[0].trim().split(",")) {
            String[] o = s.trim().split("\\s");
            ret[idMap.get(o[1].trim())] = -Integer.parseInt(o[0].trim());
        }
        return new Pair<>(outType, ret);
    }

    private static void doReactions(long[] materialCounts, long[][] reactionMap, double[] reactOutInv) {
        int mcount = materialCounts.length;
        int starti = 1;
        int endi = mcount - 1;
        for (int i = starti; i != endi; i++) {
            if (i >= mcount) i = 1;
            if (materialCounts[i] < 0) {
                long[] react = reactionMap[i];
                long times = (long) (-materialCounts[i] * reactOutInv[i]);
                if (times * reactionMap[i][i] < -materialCounts[i]) times++;
                for (int j = 0; j < react.length; j++) {
                    materialCounts[j] += times * react[j];
                }
                endi = i;
            }
        }
    }

    private static int doReactions2(long[][] reactionMap, long targetOreCount, double[] reactOutInv) {
        long[] targetCounts = new long[reactionMap.length];
        int fuelMade = 0;
        while (true) {
            targetCounts[FUEL]--;
            fuelMade++;
            doReactions(targetCounts, reactionMap, reactOutInv);
            if (targetCounts[ORE] < -targetOreCount) return fuelMade;
        }
    }
}
