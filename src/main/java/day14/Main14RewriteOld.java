package day14;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Rewrite of Main14pt2OldBad, without any of the oldbroken code, reasonably clean
 */
public class Main14RewriteOld {
    private static final int FUEL = 1;
    private static final int ORE = 0;

    public static void main(String[] args) throws IOException {
        Object2IntOpenHashMap<String> idMap = new Object2IntOpenHashMap<>();
        idMap.put("ORE", ORE);
        idMap.put("FUEL", FUEL);
        AtomicInteger id = new AtomicInteger(Math.max(FUEL, ORE) + 1);

        List<String> list = Files.readAllLines(Paths.get("run/in14.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#")).collect(Collectors.toList());
        list.stream().map(x -> x.split("=>")[1].trim().split("\\s")[1])
                .filter(x -> !x.equals("FUEL")).forEach(x -> idMap.put(x, id.getAndIncrement()));
        List<Reaction> reactions = list.stream().map(x -> x.split("=>"))
                .map(x1 -> new Reaction(idMap, x1))
                .collect(Collectors.toList());

        final int idCount = id.get();
        Reaction[] reactionMap = new Reaction[idCount];
        reactions.forEach(r -> reactionMap[r.output] = r);
        reactionMap[ORE] = new Reaction(ORE, 1, new int[]{ORE}, new int[]{1});

        int[] targetCounts = new int[idCount];
        targetCounts[FUEL] = -1;

        doReactions(targetCounts, reactionMap);
        System.out.println("Req ore: " + -targetCounts[ORE]);

        int fuelMade = doReactions2(reactionMap, 1000000000000L);
        System.out.println("Fuel made: " + fuelMade);
    }

    private static int[] doReactions(int[] targetCounts, Reaction[] reactionMap) {
        while (hasAnyMissing(targetCounts)) {
            for (int i = 0; i < targetCounts.length; i++) {
                if (i != ORE) {
                    while (targetCounts[i] < 0) {
                        reactionMap[i].apply(targetCounts);
                    }
                }
            }
        }
        return targetCounts;
    }

    private static int doReactions2(Reaction[] reactionMap, long targetOreCount) {
        long[] targetCounts = new long[reactionMap.length];
        int fuelMade = 0;
        while (true) {
            targetCounts[FUEL]--;
            fuelMade++;
            while (hasAnyMissing(targetCounts)) {
                for (int i = 0; i < targetCounts.length; i++) {
                    if (i != ORE) {
                        while (targetCounts[i] < 0) {
                            reactionMap[i].apply(targetCounts);
                            if (targetCounts[ORE] < -targetOreCount) {
                                return fuelMade;
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean hasAnyMissing(int[] targetCounts) {
        for (int i = 1; i < targetCounts.length; i++) {
            if (targetCounts[i] < 0) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasAnyMissing(long[] targetCounts) {
        for (int i = 1; i < targetCounts.length; i++) {
            if (targetCounts[i] < 0) {
                return true;
            }
        }
        return false;
    }

    private static class Reaction {
        int[] inputs;
        int[] inputCounts;
        int output;
        int outputCount;

        public Reaction(int out, int outCount, int[] inputs, int[] inputCount) {
            this.output = out;
            this.outputCount = outCount;
            this.inputs = inputs;
            this.inputCounts = inputCount;
        }

        public Reaction(Object2IntOpenHashMap<String> idMap, String[] x) {
            String[] out = x[1].trim().split("\\s");
            output = idMap.getInt(out[1]);
            outputCount = Integer.parseInt(out[0]);

            String[] in = x[0].trim().split(",");
            inputs = new int[in.length];
            inputCounts = new int[in.length];

            for (int i = 0; i < in.length; i++) {
                String[] o = in[i].trim().split("\\s");
                inputs[i] = idMap.getInt(o[1].trim());
                inputCounts[i] = Integer.parseInt(o[0].trim());
            }
        }

        public void apply(int[] counts) {
            counts[output] += outputCount;
            for (int i = 0; i < inputs.length; i++) {
                counts[inputs[i]] -= inputCounts[i];
            }
        }

        public void apply(long[] counts) {
            counts[output] += outputCount;
            for (int i = 0; i < inputs.length; i++) {
                counts[inputs[i]] -= inputCounts[i];
            }
        }
    }
}
