package day14;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import utils.IntArrPair;
import utils.Pair;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Here is the first time I had a good solution, but it's mixed with code for old broken attempts
 */
public class Main14pt2OldBad {
    private static final int FUEL = 1;
    private static final int ORE = 0;

    public static void main(String[] args) throws IOException {
        Object2IntOpenHashMap<String> idMap = new Object2IntOpenHashMap<>();
        idMap.put("ORE", ORE);
        idMap.put("FUEL", FUEL);
        AtomicInteger id = new AtomicInteger(Math.max(FUEL, ORE) + 1);

        Files.readAllLines(Paths.get("run/in14.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .map(x -> x.split("=>")[1].trim().split("\\s")[1])
                .forEach(x -> {
                    if (!x.equals("FUEL"))
                        idMap.put(x, id.getAndIncrement());
                });

        List<Reaction> reactions = Files.readAllLines(Paths.get("run/in14.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .map(x -> x.split("=>"))
                .map(x1 -> new Reaction(idMap, x1))
                .collect(Collectors.toList());

        final int maxId = id.get() - 1;

        Reaction[] reactionMap = new Reaction[maxId + 1];
        //for (int i = 0; i < reactionMap.length; i++) {
        //    reactionMap[i] = new HashSet<>();
        //}
        reactions.forEach(r -> {
            if (reactionMap[r.output] != null) {
                throw new RuntimeException();
            }
            reactionMap[r.output] = r;
        });
        reactionMap[ORE] = new Reaction(ORE, 1, new int[]{ORE}, new int[]{1});

        final long startTime = System.nanoTime();

        int[] targetCounts = new int[maxId + 1];
        targetCounts[FUEL] = -1;

        AtomicInteger requitedOre = new AtomicInteger();
        ReactionTrace[] foundTrace = new ReactionTrace[1];

        doReactions(targetCounts, reactionMap);
        System.out.println("Req ore: " + -targetCounts[ORE]);

        int fuelMade = doReactions2(reactionMap, 1000000000000L);
        System.out.println("Fuel made: " + fuelMade);
        //System.out.println(findEffectiveReaction(targetCounts, reactionMap, new int[targetCounts.length], new ReactionTrace()));

        //int fuelCount = followTrace(requitedOre.get(), foundTrace[0], reactionMap);
        //System.out.println(fuelCount);
    }

    private static int followTrace(int initialOreCount, ReactionTrace reactionTrace, Map<String, Set<Reaction>> reactionMap) {
        Map<Integer, Integer> currentMaterials = new HashMap<>();
        currentMaterials.put(ORE, initialOreCount);

        for (int i = reactionTrace.traceElements.size() - 1; i >= 0; i--) {
            Object element = reactionTrace.traceElements.get(i);
            if (element instanceof ReactionTrace.PerformReaction) {
                ReactionTrace.PerformReaction performReact = (ReactionTrace.PerformReaction) element;
                Reaction reaction = performReact.reaction;
                int reactCount = performReact.quantity;
                int outQty = reaction.outputCount * reactCount;
                int outName = 0;//performReact.outName;

                for (int j = 0; j < reaction.inputs.length; j++) {
                    int inQty = reaction.inputCounts[j] * reactCount;
                    int inName = reaction.inputs[j];
                    currentMaterials.compute(inName, (n, v) -> {
                        if (v == null) {
                            throw new RuntimeException("No element " + inName);
                        }
                        int ret = v - inQty;
                        if (ret < 0) {
                            throw new RuntimeException("Not enough element " + inName + ", missing " + -ret);
                        }
                        return ret;
                    });
                }
                currentMaterials.compute(outName, (n, v) -> (v == null ? 0 : v) + outQty);
            } else if (element instanceof ReactionTrace.ConsumeLeftovers) {
                ReactionTrace.ConsumeLeftovers consume = (ReactionTrace.ConsumeLeftovers) element;
                currentMaterials.compute(/*consume.name*/0, (n, v) -> {
                    if (v == null) {
                        throw new RuntimeException("No element " + consume.name);
                    }
                    int ret = v - consume.quantity;
                    if (ret < 0) {
                        throw new RuntimeException("Not enough element " + consume.name + ", missing " + -ret);
                    }
                    return ret;
                });
            }
        }
        return currentMaterials.getOrDefault("FUEL", 0);
    }

    static ReactionTrace bestTrace;
    static int bestCount = Integer.MAX_VALUE;
    static int[] bestLeftovers;

    static final AtomicInteger nohit = new AtomicInteger();
    private static Map<IntArrPair, Pair<Integer, ReactionTrace>> memoized = new HashMap<>();

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
        //return targetCounts;
    }

    // Pair<OreUsed, Leftovers>
    private static Pair<Integer, ReactionTrace> findEffectiveReaction(
            int[] targetCounts, Reaction[] reactionMap,
            int[] leftovers, ReactionTrace trace) {

        if (targetCounts[ORE] != 0 && hasOnlyOres(targetCounts)) {
            trace.finalLeftovers(leftovers.clone());
            return new Pair<>(targetCounts[ORE], trace);
        }
        IntArrPair key = null;
        key = new IntArrPair(targetCounts, leftovers.clone());
        Pair<Integer, ReactionTrace> memo = memoized.get(key);
        if (memo != null) {
            return new Pair<>(memo.a, new ReactionTrace(memo.b));
        }
        nohit.incrementAndGet();

        int[] src = targetCounts.clone();
        src[ORE] = 0;
        Reaction[] reactionSetList = createReactionSetList(src, reactionMap);

        ReactionTrace[] bestTrace = new ReactionTrace[1];
        int[][] bestReactLeftovers = new int[1][];
        Integer[] bestReactionSet = new Integer[1];

        Utils.forAllPermutations(reactionSetList, reactionsForAllSources -> {
            ReactionTrace currentReactTrace = new ReactionTrace(trace);
            int[] currentLeftovers = leftovers.clone();

            int totalOreUsed = targetCounts[ORE];

            for (Reaction reaction : reactionsForAllSources) {
                if (reaction == null) return;
                int output = reaction.output;
                int requiredOutputCount = targetCounts[output];
                if (requiredOutputCount == 0) {
                    continue;
                }
                int leftOut = currentLeftovers[output];
                int takenOut = Math.min(leftOut, requiredOutputCount);
                if (takenOut != 0) {
                    currentReactTrace.consumeLeftovers(output, takenOut);
                    currentLeftovers[output] -= takenOut;
                }
                requiredOutputCount -= takenOut;

                if (requiredOutputCount == 0) {
                    continue;
                }
                int reactionOutputCount = reaction.outputCount;
                int reactionAmount = (int) Math.ceil(requiredOutputCount / (double) reactionOutputCount);

                int[] requiredInputs = new int[targetCounts.length];

                for (int i = 0; i < reaction.inputs.length; i++) {
                    int input = reaction.inputs[i];
                    int inCount = reaction.inputCounts[i] * reactionAmount;
                    if (inCount != 0) {
                        requiredInputs[input] += inCount;
                    }
                }

                Pair<Integer, ReactionTrace> effectiveReaction;
                effectiveReaction = findEffectiveReaction(requiredInputs,
                        reactionMap, currentLeftovers, currentReactTrace);

                if (effectiveReaction == null) {
                    System.out.println("NULL? " + Arrays.toString(requiredInputs));
                    return;
                }
                currentLeftovers = effectiveReaction.b.left;
                currentReactTrace = effectiveReaction.b;
                totalOreUsed += effectiveReaction.a;

                int leftoverOutput = reactionAmount * reactionOutputCount - requiredOutputCount;
                currentReactTrace.performReaction(output, reactionAmount);
                if (leftoverOutput != 0) {
                    currentReactTrace.putLeftovers(output, leftoverOutput);
                    currentLeftovers[output] += leftoverOutput;
                }
            }
            if (bestReactionSet[0] == null || totalOreUsed < bestReactionSet[0]) {
                bestReactionSet[0] = totalOreUsed;
                bestReactLeftovers[0] = currentLeftovers;
                bestTrace[0] = currentReactTrace;
                bestTrace[0].finalLeftovers(bestReactLeftovers[0]);
                if (totalOreUsed < bestCount) {

                    if (targetCounts[FUEL] != 0) {
                        boolean noOther = true;
                        for (int i = 0; i < targetCounts.length; i++) {
                            int targetCount = targetCounts[i];
                            if (i != FUEL && targetCount != 0) noOther = false;
                        }
                        if (noOther) {
                            bestCount = totalOreUsed;
                            bestLeftovers = currentLeftovers;
                            Main14pt2OldBad.bestTrace = currentReactTrace;
                            System.out.println("NEWBEST: " + bestCount);
                            System.out.println("LEFT   : " + Arrays.toString(bestLeftovers));
                            //System.out.println(currentReactTrace);
                        }
                    }
                }
            }
        });
        if (bestReactionSet[0] == null) {
            return null;
        }
        //System.out.println("OREUSED: " + bestReactionSet + " src=" + sourceAndCount);
        Pair<Integer, ReactionTrace> ret = new Pair<>(bestReactionSet[0], bestTrace[0]);
        if (memoized.size() > 1000000) {
            memoized.clear();
            nohit.set(0);
            System.out.println("CLEAR");
        }
        memoized.put(key, new Pair<>(bestReactionSet[0], new ReactionTrace(bestTrace[0])));
        return ret;
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

    private static boolean hasOnlyOres(int[] targetCounts) {
        for (int i = 1; i < targetCounts.length; i++) {
            if (targetCounts[i] != 0) {
                return false;
            }
        }
        return true;
    }

    private static List<List<Reaction>> createPermutations(List<Set<Reaction>> reactionSetList) {
        Iterator<ArrayList<Reaction>> iterator = Utils.permutations(reactionSetList, ArrayList::new);
        List<List<Reaction>> permutations = new ArrayList<>();
        iterator.forEachRemaining(e -> permutations.addAll(Utils.permutations(e)));
        return permutations;
    }

    private static Reaction[] createReactionSetList(int[] targets, Reaction[] reactionMap) {
        int cnt = 0;
        for (int target : targets) {
            if (target != 0) cnt++;
        }
        Reaction[] out = new Reaction[cnt];
        for (int i = 0, j = 0; i < targets.length; i++) {
            if (targets[i] != 0) {
                out[j++] = reactionMap[i];
            }
        }
        return out;
    }

    private static Reaction mergeReactions(Reaction r1, Reaction r2) {
        if (r1.output != r2.output) {
            throw new IllegalArgumentException(r1 + ", " + r2);
        }
        Map<Integer, Integer> inCounts = new HashMap<>();
        for (int i = 0; i < r1.inputs.length; i++) {
            int fi = i;
            inCounts.compute(r1.inputs[i], (n, p) -> p == null ? r1.inputCounts[fi] : p + r1.inputCounts[fi]);
        }
        for (int i = 0; i < r2.inputs.length; i++) {
            int fi = i;
            inCounts.compute(r2.inputs[i], (n, p) -> p == null ? r2.inputCounts[fi] : p + r2.inputCounts[fi]);
        }
        return new Reaction(r1.output, r1.outputCount + r2.outputCount,
                inCounts.keySet().stream().mapToInt(x -> x).toArray(), inCounts.values().stream().mapToInt(x -> x).toArray());
    }

    // reversed reaction trace
    private static class ReactionTrace {

        final IntList traceElements = new IntArrayList(64);
        private int[] left;

        public ReactionTrace() {
        }

        public ReactionTrace(ReactionTrace trace) {
            this.traceElements.addAll(trace.traceElements);
            if (trace.left != null) {
                this.left = trace.left.clone();
            }
        }

        void consumeLeftovers(int name, int qty) {
            traceElements.add(0);
            traceElements.add(name);
            traceElements.add(qty);
        }

        void putLeftovers(int name, int qty) {
            traceElements.add(1);
            traceElements.add(name);
            traceElements.add(qty);
        }

        void performReaction(int output, int qty) {
            traceElements.add(0);
            traceElements.add(output);
            traceElements.add(qty);
        }

        @Override
        public String toString() {
            return "ReactionTrace{\n    " +
                    traceElements.stream().map(Object::toString).collect(Collectors.joining("\n    ")) +
                    "\n}\nfinalLeftovers=" + Arrays.toString(left);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReactionTrace that = (ReactionTrace) o;
            return Objects.equals(traceElements, that.traceElements) &&
                    Arrays.equals(left, that.left);
        }

        @Override
        public int hashCode() {
            return Objects.hash(traceElements, left);
        }

        public void finalLeftovers(int[] left) {
            this.left = left;
        }

        static class ConsumeLeftovers {
            final String name;
            final int quantity;

            ConsumeLeftovers(String name, int quantity) {
                this.name = name;
                this.quantity = quantity;
            }

            @Override
            public String toString() {
                return "ConsumeLeftovers{" +
                        "name='" + name + '\'' +
                        ", quantity=" + quantity +
                        '}';
            }
        }

        static class PutLeftovers {
            final String name;
            final int quantity;
            final Reaction fromReaction;

            PutLeftovers(String name, int quantity, Reaction fromReaction) {
                this.name = name;
                this.quantity = quantity;
                this.fromReaction = fromReaction;
            }

            @Override
            public String toString() {
                return "PutLeftovers{" +
                        "name='" + name + '\'' +
                        ", quantity=" + quantity +
                        ", fromReaction=" + fromReaction +
                        '}';
            }
        }

        static class PerformReaction {
            final String outName;
            final int quantity;
            final Reaction reaction;

            PerformReaction(String outName, int quantity, Reaction forReaction) {
                this.outName = outName;
                this.quantity = quantity;
                this.reaction = forReaction;
            }

            @Override
            public String toString() {
                return "PerformReaction{" +
                        "outName='" + outName + '\'' +
                        ", quantity=" + quantity +
                        ", reaction=" + reaction +
                        '}';
            }
        }
    }

    private static class Material {
        final int count;
        final int material;

        private Material(int count, int material) {
            this.count = count;
            this.material = material;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Material material1 = (Material) o;
            return count == material1.count &&
                    material == material1.material;
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, material);
        }
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

        public Reaction(Material output, Material... inputs) {
            this.outputCount = output.count;
            this.output = output.material;

            this.inputs = new int[inputs.length];
            this.inputCounts = new int[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                Material input = inputs[i];
                this.inputs[i] = input.material;
                this.inputCounts[i] = input.count;
            }
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

        @Override
        public String toString() {
            return "Reaction{" +
                    "inputs=" + Arrays.toString(inputs) +
                    ", inputCounts=" + Arrays.toString(inputCounts) +
                    ", output='" + output + '\'' +
                    ", outputCount=" + outputCount +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Reaction reaction = (Reaction) o;
            return output == reaction.output &&
                    outputCount == reaction.outputCount &&
                    Arrays.equals(inputs, reaction.inputs) &&
                    Arrays.equals(inputCounts, reaction.inputCounts);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(output, outputCount);
            result = 31 * result + Arrays.hashCode(inputs);
            result = 31 * result + Arrays.hashCode(inputCounts);
            return result;
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

    /*

    private static Pair<Reaction, Map<String, Integer>> findEffectiveReaction(
            String source, int count, Map<String, Set<Reaction>> reactionMap,
            Map<String, Integer> leftovers) {
        System.out.println("FINDING " + source);
        if (source.equals("ORE")) {
            int inCnt = count;
            if (leftovers.containsKey("ORE")) {
                int left = leftovers.get("ORE");
                if (left > inCnt) {
                    leftovers.put("ORE", left - inCnt);
                    inCnt = 0;
                } else {
                    leftovers.remove("ORE");
                    inCnt -= left;
                }
            }
            System.out.println("RET " + source);
            return new Pair<>(new Reaction(new Material(inCnt, "ORE"), new Material(count, "ORE")), leftovers);
        }
        Set<Reaction> allReactions = reactionMap.get(source);
        if (allReactions == null) {
            System.out.println("RETNULL " + source);
            return null;
        }

        Map<String, Set<Reaction>> newReactMap = new HashMap<>(reactionMap);
        //newReactMap.remove(source);

        Map<String, Integer> bestReactLeftovers = new HashMap<>(leftovers);
        Reaction bestReaction = null;
        reactionsSearch:
        for (Reaction react : allReactions) {
            int factor = 1; // how many of this reaction needs to happen to produce at least the needed amount
            while (react.outputCount * factor < count) {
                factor++;
            }
            int leftoverOutput = react.outputCount * factor - count;

            Map<String, Integer> currentLeftovers = new HashMap<>(leftovers);
            Reaction r;

            List<Reaction> toCombine = new ArrayList<>();
            for (int i = 0; i < react.inputs.length; i++) {
                int currentInCount = react.inputCounts[i] * factor;
                if (currentLeftovers.containsKey(react.inputs[i])) {
                    int leftover = currentLeftovers.get(react.inputs[i]);
                    System.out.println("Found " + leftover + " left for " + react.inputs[i]);
                    if (leftover >= currentInCount) {
                        currentLeftovers.put(react.inputs[i], leftover - currentInCount);
                        currentInCount = 0;
                    } else {
                        currentLeftovers.remove(react.inputs[i]);
                        currentInCount -= leftover;
                    }
                }

                Pair<Reaction, Map<String, Integer>> bestInReact = findEffectiveReaction(react.inputs[i],
                        currentInCount, newReactMap, currentLeftovers);
                if (bestInReact == null) {
                    continue reactionsSearch;
                }
                currentLeftovers = bestInReact.b;
                toCombine.add(bestInReact.a);
            }

            currentLeftovers.compute(react.output, (n, cnt) -> cnt == null ? leftoverOutput : (cnt + leftoverOutput));
            r = combineReactions(react, count, toCombine, currentLeftovers);

            if (r.inputs.length == 1 && r.inputs[0].equals("ORE")
                    && (bestReaction == null || r.inputCounts[0] < bestReaction.inputCounts[0])) {
                bestReaction = r;
                bestReactLeftovers = currentLeftovers;
            }
        }
        if (bestReaction == null) {
            System.out.println("RETNULL2 " + source);
            return null;
        }
        System.out.println("RET " + source + ", CNT=" + count + ", REACT=" + bestReaction + ", LEFT=" + bestReactLeftovers);
        return new Pair<>(bestReaction, bestReactLeftovers);
    }

    private static Reaction combineReactions(Reaction targetReact, int count, List<Reaction> toCombine, Map<String, Integer> currentLeftovers) {
        int factor = 1;
        // int outCount = targetReact.outputCount;
        // while (outCount * factor < count) {
        //     factor++;
        // }

        //  if (outCount * factor > count) {
        //      int v = currentLeftovers.getOrDefault(targetReact.output, 0);
        //      v += outCount * factor - count;
        //      currentLeftovers.put(targetReact.output, v);
        //  }

        Map<String, Integer> targetReactInputCounts = new HashMap<>();
        for (int i = 0; i < targetReact.inputs.length; i++) {
            targetReactInputCounts.put(targetReact.inputs[i], targetReact.inputCounts[i]);
        }

        Map<String, Integer> inputs = new HashMap<>();
        System.out.println("COMBINE: factor=" + factor + ", count=" + count + ", target=" + targetReact + ", react=" + Arrays.toString(toCombine.toArray()));
        for (Reaction reaction : toCombine) {
            String[] strings = reaction.inputs;
            for (int i = 0; i < strings.length; i++) {
                String input = strings[i];
                Integer in = inputs.get(input);
                if (in == null) {
                    in = 0;
                }
                in += reaction.inputCounts[i] * factor;
                inputs.put(input, in);
            }
        }

        Material[] inputMaterials = inputs.entrySet().stream().map(e -> new Material(e.getValue(), e.getKey()))
                .toArray(Material[]::new);
        Reaction reaction = new Reaction(new Material(targetReact.outputCount * factor, targetReact.output), inputMaterials);
        System.out.println("COMBINEDREACT: " + reaction);
        return reaction;
    }

    */
}
