package day14;

import utils.Pair;
import utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This shows what remained of my old broken attempts.
 * These are very broken and this is nowhere near how it should be done.
 * But cn be used to arrive at the right answer for part1, if any of this still works
 */
public class Main14OldBadBrokenAttempts {
    public static void main(String[] args) throws IOException {
        List<Reaction> reactions = Files.readAllLines(Paths.get("run/in14.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .map(x -> x.split("=>"))
                .map(Reaction::new)
                .collect(Collectors.toList());

        Map<String, Set<Reaction>> reactionMap = reactions.stream()
                .collect(Collectors.groupingBy(r -> r.output, Collectors.toSet()));
        reactionMap.computeIfAbsent("ORE", n -> Collections.singleton(new Reaction("ORE", 1,
                new String[]{"ORE"}, new int[]{1})));

        Map<String, Integer> src = new HashMap<>();
        src.put("FUEL", 1);
        System.out.println(findEffectiveReaction(src, reactionMap, new HashMap<>(), new AtomicInteger(999999999)));
    }

    private static Pair<Reaction, Map<String, Integer>> findEffectiveReaction(
            Map<String, Integer> sourceAndCount, Map<String, Set<Reaction>> reactionMap,
            Map<String, Integer> leftovers, AtomicInteger bestSoFar) {

        List<Set<Map.Entry<String, Reaction>>> reactionSetList = reactionMap.entrySet()
                .stream()
                .filter(x -> sourceAndCount.containsKey(x.getKey()))
                .map(x -> x.getValue().stream()
                        .map(r -> (Map.Entry<String, Reaction>) new SimpleEntry<>(x.getKey(), r))
                        .collect(Collectors.toSet())
                ).collect(Collectors.toList());
        // simulates a boolean, telling whether a term should be expand
        for (Set<Map.Entry<String, Reaction>> entries : reactionSetList) {
            Set<Map.Entry<String, Reaction>> n = new HashSet<>();
            for (Map.Entry<String, Reaction> entry : entries) {
                n.add(new SimpleEntry<>(entry.getKey() + "$NOEXPAND", entry.getValue()));
            }
            entries.addAll(n);
        }

        Iterator<ArrayList<Map.Entry<String, Reaction>>> iterator = Utils.permutations(reactionSetList, ArrayList::new);

        Map<String, Integer> bestReactLeftovers = null;
        Reaction bestReactionSet = null;

        while (iterator.hasNext()) {
            Map<String, Reaction> reactionsForAllSources = iterator.next().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Main14OldBadBrokenAttempts::mergeReactions));

            if (reactionsForAllSources.keySet().stream().filter(x -> !x.equals("ORE") && !x.equals("ORE$NOEXPAND"))
                    .allMatch(x -> x.endsWith("$NOEXPAND"))) {
                continue; // we have to expand *something* on each step
            }
            Map<String, Integer> currentLeftovers = new HashMap<>(leftovers);
            Map<String, Integer> requiredInputs = new HashMap<>();

            boolean anyNonOre = false;
            for (Map.Entry<String, Reaction> stringReactionEntry : reactionsForAllSources.entrySet()) {
                String output = stringReactionEntry.getKey();
                if (output.endsWith("$NOEXPAND")) {
                    String name = output.split("\\$")[0];
                    if (!name.equals("ORE")) {
                        anyNonOre = true;
                    }
                    int reqCount = sourceAndCount.get(name);
                    int left = currentLeftovers.getOrDefault(name, 0);
                    int taken = Math.min(left, reqCount);
                    currentLeftovers.compute(name, (n, v) -> (v == null ? 0 : v) - taken);
                    requiredInputs.compute(name, (n, j) -> (j == null ? 0 : j) + reqCount - taken);
                    continue;
                }
                if (output.equals("ORE")) {
                    int reqCount = sourceAndCount.get("ORE");
                    requiredInputs.compute("ORE", (n, i) -> (i == null ? 0 : i) + reqCount);
                    continue;
                }

                Reaction reaction = stringReactionEntry.getValue();
                int reactionOutputCount = reaction.outputCount;
                int requiredOutputCount = sourceAndCount.get(output);
                int reactionAmount = (int) Math.ceil(requiredOutputCount / (double) reactionOutputCount);
                int leftoverOutput = reactionAmount * reactionOutputCount - requiredOutputCount;

                for (int i = 0; i < reaction.inputs.length; i++) {
                    String input = reaction.inputs[i];
                    if (!input.equals("ORE")) {
                        anyNonOre = true;
                    }
                    int inCount = reaction.inputCounts[i] * reactionAmount;
                    int left = currentLeftovers.getOrDefault(input, 0);
                    int taken = Math.min(left, inCount);
                    currentLeftovers.compute(input, (n, v) -> (v == null ? 0 : v) - taken);
                    requiredInputs.compute(input, (n, j) -> (j == null ? 0 : j) + inCount - taken);
                }
                currentLeftovers.compute(output, (n, v) -> (v == null ? 0 : v) + leftoverOutput);
            }
            Pair<Reaction, Map<String, Integer>> effectiveReaction;
            if (anyNonOre) {
                effectiveReaction = findEffectiveReaction(requiredInputs, reactionMap, currentLeftovers, bestSoFar);
            } else {
                int reqOre = requiredInputs.get("ORE");
                if (reqOre < bestSoFar.get()) {
                    bestSoFar.set(reqOre);
                    System.out.println("NEWBEST: " + reqOre + ", left=" + currentLeftovers);
                }
                effectiveReaction = new Pair<>(new Reaction("ORE", sourceAndCount.get("ORE"),
                        new String[]{"ORE"}, new int[]{reqOre}), currentLeftovers);
            }
            if (effectiveReaction == null) {
                //System.out.println("NULL? " + requiredInputs);
                continue;
            }
            if (bestReactionSet == null || effectiveReaction.a.inputCounts[0] < bestReactionSet.inputCounts[0]) {
                bestReactionSet = effectiveReaction.a;
                bestReactLeftovers = effectiveReaction.b;
            }
        }
        if (bestReactionSet == null) {
            return null;
        }
        return new Pair<>(bestReactionSet, bestReactLeftovers);
    }

    private static Reaction mergeReactions(Reaction r1, Reaction r2) {
        if (!r1.output.equals(r2.output)) {
            throw new IllegalArgumentException(r1 + ", " + r2);
        }
        Map<String, Integer> inCounts = new HashMap<>();
        for (int i = 0; i < r1.inputs.length; i++) {
            int fi = i;
            inCounts.compute(r1.inputs[i], (n, p) -> p == null ? r1.inputCounts[fi] : p + r1.inputCounts[fi]);
        }
        for (int i = 0; i < r2.inputs.length; i++) {
            int fi = i;
            inCounts.compute(r2.inputs[i], (n, p) -> p == null ? r2.inputCounts[fi] : p + r2.inputCounts[fi]);
        }
        return new Reaction(r1.output, r1.outputCount + r2.outputCount,
                inCounts.keySet().toArray(new String[0]), inCounts.values().stream().mapToInt(x -> x).toArray());
    }

    private static class ReactionTrace {
        
    }
    private static class Material {
        final int count;
        final String material;

        private Material(int count, String material) {
            this.count = count;
            this.material = material;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Material that = (Material) o;
            return count == that.count &&
                    Objects.equals(material, that.material);
        }

        @Override
        public int hashCode() {
            return Objects.hash(count, material);
        }
    }

    private static class Reaction {
        String[] inputs;
        int[] inputCounts;

        String output;
        int outputCount;

        public Reaction(String out, int outCount, String[] inputs, int[] inputCount) {
            this.output = out;
            this.outputCount = outCount;
            this.inputs = inputs;
            this.inputCounts = inputCount;
        }

        public Reaction(Material output, Material... inputs) {
            this.outputCount = output.count;
            this.output = output.material;

            this.inputs = new String[inputs.length];
            this.inputCounts = new int[inputs.length];
            for (int i = 0; i < inputs.length; i++) {
                Material input = inputs[i];
                this.inputs[i] = input.material;
                this.inputCounts[i] = input.count;
            }
        }

        public Reaction(String[] x) {
            String[] out = x[1].trim().split("\\s");
            output = out[1];
            outputCount = Integer.parseInt(out[0]);

            String[] in = x[0].trim().split(",");
            inputs = new String[in.length];
            inputCounts = new int[in.length];

            for (int i = 0; i < in.length; i++) {
                String[] o = in[i].trim().split("\\s");
                inputs[i] = o[1];
                inputCounts[i] = Integer.parseInt(o[0]);
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
            return outputCount == reaction.outputCount &&
                    Arrays.equals(inputs, reaction.inputs) &&
                    Arrays.equals(inputCounts, reaction.inputCounts) &&
                    output.equals(reaction.output);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(output, outputCount);
            result = 31 * result + Arrays.hashCode(inputs);
            result = 31 * result + Arrays.hashCode(inputCounts);
            return result;
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
