package day20;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import utils.Pair;
import utils.Position2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

public class Main20 {
    public static void main(String[] args) throws IOException {
        byte[][] map = Files.readAllLines(Paths.get("run/in20.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("//"))
                .map(s -> s.getBytes(StandardCharsets.US_ASCII))
                .toArray(byte[][]::new);

        Map<Position2, Character> charMap = new HashMap<>();

        int maxX = map.length;
        int maxYT = 0;
        Map<String, Set<Position2>> portals = new HashMap<>();
        for (int i = 0; i < map.length; i++) {
            if (map[i].length > maxYT) {
                maxYT = map[i].length;
            }
            for (int j = 0; j < map[i].length; j++) {
                byte b = map[i][j];
                if (b != '#' && b != ' ') {
                    Position2 pos = new Position2(i, j);
                    charMap.put(pos, (char) b);
                }
            }
        }
        int maxY = maxYT;

        charMap.forEach((pos, chr) -> {
            if (chr != '.') {
                Pair<String, Position2> portal = getPortal(pos, charMap);
                portals.computeIfAbsent(portal.a, x -> new HashSet<>()).add(portal.b);
            }
        });

        Map<String, Portal> portalsByName = new HashMap<>();
        portals.forEach((name, positions) -> {
            if (portalsByName.containsKey(name)) return;
            System.out.println("Making portal " + name);
            Portal portal = new Portal(name);
            portalsByName.put(name, portal);
        });
        portalsByName.values().forEach(portal -> computeReachable(portal, portals, charMap, portalsByName));
        portalsByName.values().forEach(portal -> computeDirectlyReachable(portal, portals, charMap, portalsByName, maxX, maxY, true));
        portalsByName.values().forEach(portal -> computeDirectlyReachable(portal, portals, charMap, portalsByName, maxX, maxY, false));

        for (Portal portal : portalsByName.values()) {
            System.out.print("Portal " + portal.name + ": ");
            portal.reachablePortals.forEach((reachable, distance) -> System.out.printf("(%s -> %d)  ", reachable.name, distance));
            System.out.println();
        }
        System.out.println();
        Object2IntOpenHashMap<Portal> reachableFromA = portalsByName.get("AA").reachablePortals;
        int distZZ = reachableFromA.object2IntEntrySet().stream().filter(e -> e.getKey().name.equals("ZZ"))
                .mapToInt(Object2IntMap.Entry::getIntValue).findAny().orElse(-1);
        System.out.println("Part1 = AA->ZZ: " + distZZ);

        dijkstraShortestPath(new Pair<>(portalsByName.get("AA"), 0), new Pair<>(portalsByName.get("ZZ"), 0));
    }

    private static Pair<String, Position2> getPortal(Position2 pos, Map<Position2, Character> charMap) {
        List<Character> name = new ArrayList<>();

        Set<Position2> portalPositions = new HashSet<>();
        portalPositions.add(pos);

        Set<Position2> visited = new HashSet<>();

        Queue<Position2> posQueue = new ArrayDeque<>();
        posQueue.add(pos);
        while (!posQueue.isEmpty()) {
            Position2 p = posQueue.poll();
            if (visited.contains(p)) continue;
            visited.add(p);
            for (Direction value : Direction.values()) {
                Position2 move = value.move(p);
                Character c = charMap.get(move);
                if (c != null && c != '.') {
                    portalPositions.add(move);
                    name.add(c);
                    posQueue.add(move);
                }
            }
        }

        name.sort(Comparator.comparing(c -> c));
        StringBuilder sb = new StringBuilder(name.size());
        name.forEach(sb::append);

        for (Position2 portalPos : portalPositions) {
            for (Direction value : Direction.values()) {
                Position2 move = value.move(portalPos);
                Character c = charMap.get(move);
                if (c != null && c == '.') {
                    return new Pair<>(sb.toString(), move);
                }
            }
        }
        throw new RuntimeException();
    }

    private static void computeReachable(Portal portal, Map<String, Set<Position2>> portals, Map<Position2, Character> charMap,
                                         Map<String, Portal> portalsByName) {
        Set<Position2> filled = new HashSet<>(portals.get(portal.name));
        Set<Position2> filledPortals = new HashSet<>(portals.get(portal.name));
        Set<Position2> toCheck = new HashSet<>(portals.get(portal.name));

        int distance = 0;
        while (!toCheck.isEmpty()) {
            Set<Position2> newToCheck = new HashSet<>();
            for (Position2 pos : toCheck) {
                for (Direction dir : Direction.values()) {
                    Position2 moved = dir.move(pos);
                    if (filled.contains(moved) || filledPortals.contains(moved)) {
                        continue;
                    }
                    filled.add(moved);
                    Character c = charMap.get(moved);
                    if (c != null) {
                        if (c == '.') {
                            newToCheck.add(moved);
                        } else {
                            filledPortals.add(moved);
                            String portalName = getPortal(moved, charMap).a;
                            Portal otherPortal = portalsByName.get(portalName);
                            Set<Position2> outPositions = portals.get(otherPortal.name);
                            portal.addReachable(otherPortal, distance);

                            for (Position2 p : outPositions) {
                                if (!p.equals(pos)) {
                                    newToCheck.add(p);
                                }
                            }
                        }
                    }
                }
            }
            distance++;
            toCheck = newToCheck;
        }
    }

    private static void computeDirectlyReachable(Portal portal, Map<String, Set<Position2>> portals, Map<Position2, Character> charMap,
                                                 Map<String, Portal> portalsByName, int maxX, int maxY, boolean isInner) {
        Set<Position2> portalPos = portals.get(portal.name).stream()
                .filter(p -> {
                    boolean isOuter = p.x < 4 || p.x > (maxX - 4)
                            || p.y < 4 || p.y > (maxY - 4);
                    return isInner != isOuter;
                }).collect(Collectors.toSet());
        Set<Position2> filled = new HashSet<>(portalPos);
        Set<Position2> filledPortals = new HashSet<>(portalPos);
        Set<Position2> toCheck = new HashSet<>(portalPos);

        int distance = 0;
        while (!toCheck.isEmpty()) {
            Set<Position2> newToCheck = new HashSet<>();
            for (Position2 pos : toCheck) {
                for (Direction dir : Direction.values()) {
                    Position2 moved = dir.move(pos);
                    if (filled.contains(moved) || filledPortals.contains(moved)) {
                        continue;
                    }
                    filled.add(moved);
                    Character c = charMap.get(moved);
                    if (c != null) {
                        if (c == '.') {
                            newToCheck.add(moved);
                        } else {
                            filledPortals.add(moved);
                            String portalName = getPortal(moved, charMap).a;
                            Portal otherPortal = portalsByName.get(portalName);

                            if (distance != 0 && distance != 1) {
                                boolean isOuter = moved.x < 4 || moved.x > (maxX - 4)
                                        || moved.y < 4 || moved.y > (maxY - 4);
                                Map<Portal, Pair<Integer, Integer>> directlyReachable = isInner ? portal.directlyReachableFromInner
                                        : portal.directlyReachableFromOuter;

                                if (!otherPortal.name.equals("AA") && !otherPortal.name.equals("ZZ")) {
                                    // inner and outer are flipped because we are assuming teleporting
                                    directlyReachable.put(otherPortal, new Pair<>(distance, isOuter ? -1 : 1));
                                } else {
                                    directlyReachable.put(otherPortal, new Pair<>(distance, 0));
                                }
                            }
                        }
                    }
                }
            }
            distance++;
            toCheck = newToCheck;
        }
    }

    // adaptation of https://stackabuse.com/graphs-in-java-dijkstras-algorithm/
    public static void dijkstraShortestPath(Pair<Portal, Integer> start, Pair<Portal, Integer> end) {
        HashSet<Pair<PortalSide, Integer>> visited = new HashSet<>();
        HashMap<Pair<PortalSide, Integer>, Pair<PortalSide, Integer>> currentPath = new HashMap<>();
        Pair<PortalSide, Integer> startSpecific = new Pair<>(new PortalSide(start.a, false), 0);
        Pair<PortalSide, Integer> endSpecific = new Pair<>(new PortalSide(end.a, false), 0);
        currentPath.put(startSpecific, null);

        HashMap<Pair<PortalSide, Integer>, Integer> distanceMap = new HashMap<>();
        distanceMap.put(startSpecific, 0);

        for (Map.Entry<Portal, Pair<Integer, Integer>> edgeEntry : start.a.directlyReachableFromOuter.entrySet()) {
            boolean destInner = edgeEntry.getValue().b == -1;
            int newLevel = start.b + edgeEntry.getValue().b;
            if (newLevel < 0) continue;
            Pair<PortalSide, Integer> dest = new Pair<>(new PortalSide(edgeEntry.getKey(), destInner), newLevel);
            distanceMap.put(dest, edgeEntry.getValue().a);
            currentPath.put(dest, startSpecific);
        }

        visited.add(startSpecific);

        while (true) {
            Pair<PortalSide, Integer> current = nextClosestReachable(distanceMap, visited);

            if (current == null) {
                System.out.println("There is no path between " + start + " and " + end);
                return;
            }
            boolean currentNodeInner = current.a.inner;

            if (current.equals(endSpecific)) {
                System.out.println("Shortest path between " + start + " and " + end + ":");
                Pair<PortalSide, Integer> child = endSpecific;
                String pathStr = end.toString();
                while (true) {
                    Pair<PortalSide, Integer> parent = currentPath.get(child);
                    if (parent == null) break;
                    pathStr = parent + " " + pathStr;
                    child = parent;
                }
                System.out.println("Part2 Path = " + pathStr);
                System.out.println("Part2 = The path costs: " + distanceMap.get(endSpecific));
                return;
            }
            visited.add(current);

            Map<Portal, Pair<Integer, Integer>> reachable = currentNodeInner ? current.a.portal.directlyReachableFromInner : current.a.portal.directlyReachableFromOuter;
            for (Map.Entry<Portal, Pair<Integer, Integer>> edgeEntry : reachable.entrySet()) {
                String name = edgeEntry.getKey().name;
                if (current.b != 0 && (name.equals("AA") || name.equals("ZZ"))) {
                    continue; // can't reach AA or ZZ from any nonzero level
                }
                int newLevel = current.b + edgeEntry.getValue().b;
                if (newLevel < 0) {
                    continue;
                }
                Pair<PortalSide, Integer> dest = new Pair<>(new PortalSide(edgeEntry.getKey(), edgeEntry.getValue().b == -1),
                        newLevel);
                if (visited.contains(dest))
                    continue;

                if (distanceMap.getOrDefault(current, Integer.MAX_VALUE / 2)
                        + edgeEntry.getValue().a + 1
                        < distanceMap.getOrDefault(dest, Integer.MAX_VALUE / 2)) {
                    distanceMap.put(dest,
                            distanceMap.getOrDefault(current, Integer.MAX_VALUE / 2) + edgeEntry.getValue().a + 1);
                    currentPath.put(dest, current);
                }
            }
        }
    }

    private static Pair<PortalSide, Integer> nextClosestReachable(HashMap<Pair<PortalSide, Integer>, Integer> shortestPathMap, HashSet<Pair<PortalSide, Integer>> visited) {
        int shortestDistance = Integer.MAX_VALUE;
        Pair<PortalSide, Integer> closestReachableNode = null;
        for (Pair<PortalSide, Integer> node : shortestPathMap.keySet()) {
            if (visited.contains(node)) continue;
            int currentDistance = shortestPathMap.get(node);
            if (currentDistance < shortestDistance) {
                shortestDistance = currentDistance;
                closestReachableNode = node;
            }
        }
        if (shortestDistance == Integer.MAX_VALUE) {
            return null;
        }
        return closestReachableNode;
    }

    private static class PortalSide {
        final Portal portal;
        final boolean inner;

        private PortalSide(Portal portal, boolean inner) {
            this.portal = portal;
            this.inner = inner;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PortalSide that = (PortalSide) o;
            return inner == that.inner &&
                    portal.equals(that.portal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(portal, inner);
        }

        @Override
        public String toString() {
            return portal + ":" + (inner ? 'I' : 'O');
        }
    }

    private static class Portal {
        private String name;
        private final Object2IntOpenHashMap<Portal> reachablePortals = new Object2IntOpenHashMap<>();
        private final Map<Portal, Pair<Integer, Integer>> directlyReachableFromOuter = new HashMap<>();
        private final Map<Portal, Pair<Integer, Integer>> directlyReachableFromInner = new HashMap<>();

        public Portal(String name) {
            this.name = name;
        }

        public void addReachable(Portal portal, int distance) {
            reachablePortals.put(portal, distance);
        }

        @Override
        public String toString() {
            return name;
        }
    }


    enum Direction {
        DOWN(0, 1),
        UP(0, -1),
        RIGHT(1, 0),
        LEFT(-1, 0);

        private final int dx;
        private final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        public Position2 move(Position2 pos) {
            return pos.add(dx, dy);
        }
    }

}
