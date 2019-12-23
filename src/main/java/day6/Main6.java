package day6;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main6 {
    public static void main(String[] args) throws IOException {
        List<String[]> data = Files.readAllLines(Paths.get("run/in6.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .map(x -> x.split("\\)")).collect(Collectors.toList());

        Map<String, Node> nodes = new HashMap<>();
        for (String[] e : data) {
            nodes.computeIfAbsent(e[0], Node::new);
            nodes.computeIfAbsent(e[1], Node::new);
            nodes.get(e[0]).orbitingObjects.add(nodes.get(e[1]));
            nodes.get(e[1]).parent = nodes.get(e[0]);
        }

        Node com = nodes.get("COM");
        int orbits = computeOrbits(com, 0);
        System.out.println("Part1 = " + orbits);

        Node meOrbit = nodes.get("YOU").parent;
        Node sanOrbit = nodes.get("SAN").parent;

        int dist = computeDistance(meOrbit, sanOrbit);
        System.out.println("Part2 = " + dist);
    }

    private static int computeDistance(Node meOrbit, Node sanOrbit) {
        // first common node
        int j = 0;
        while (true) {
            int i = 0;
            Node me = meOrbit;
            while (me.parent != null) {
                if (me.name.equals(sanOrbit.name)) {
                    return i + j;
                }
                me = me.parent;
                i++;
            }
            sanOrbit = sanOrbit.parent;
            j++;
        }
    }

    private static int computeOrbits(Node n, int depth) {
        int num = depth;
        for (Node obj : n.orbitingObjects) {
            num += computeOrbits(obj, depth + 1);
        }
        return num;
    }

    private static class Node {
        final String name;
        final List<Node> orbitingObjects = new ArrayList<>();
        Node parent;

        private Node(String name) {
            this.name = name;
        }
    }
}
