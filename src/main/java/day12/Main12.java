package day12;

import utils.Vector3;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static java.lang.Math.abs;

public class Main12 {
    public static void main(String[] args) {
        Vector3[] moons = new Vector3[]{
                new Vector3(13, 9, 5),
                new Vector3(8, 14, -2),
                new Vector3(-5, 4, 11),
                new Vector3(2, -6, 1)//*/
                /*new Vector3(-1, 0, 2),
                new Vector3(2, -10, -7),
                new Vector3(4, -8, 8),
                new Vector3(3, 5, -1)//*/
                /*new Vector3(-8, -10, 0),
                new Vector3(5, 5, 10),
                new Vector3(2, -7, 3),
                new Vector3(9, -8, -3)//*/
        };


        Vector3[] p = new Vector3[moons.length];
        System.arraycopy(moons, 0, p, 0, p.length);
        Vector3[] v = new Vector3[p.length];
        Arrays.fill(v, new Vector3(0, 0, 0));

        int stepCount = 1000;

        part1(p, v, stepCount);
        System.arraycopy(moons, 0, p, 0, p.length);
        Arrays.fill(v, new Vector3(0, 0, 0));

        part2(p, v);
    }

    private static void part2(Vector3[] p, Vector3[] v) {
        int[] x = new int[]{p[0].x, p[1].x, p[2].x, p[3].x};
        int[] vx = new int[x.length];
        int rx = findRepeatCoord(p, x, vx);

        int[] y = new int[]{p[0].y, p[1].y, p[2].y, p[3].y};
        int[] vy = new int[y.length];
        int ry = findRepeatCoord(p, y, vy);

        int[] z = new int[]{p[0].z, p[1].z, p[2].z, p[3].z};
        int[] vz = new int[z.length];
        int rz = findRepeatCoord(p, z, vz);

        System.out.println("X loop: " + rx);
        System.out.println("Y loop: " + ry);
        System.out.println("Z loop: " + rz);
    }

    private static int findRepeatCoord(Vector3[] p, int[] x, int[] vx) {
        int step = 0;
        Set<State> states = new HashSet<>();
        states.add(new State(
                x[0], x[1], x[2], x[3], vx[0], vx[1], vx[2], vx[3]
        ));
        while (true) {
            for (int j1 = 0; j1 < p.length; j1++) {
                for (int j2 = 0; j2 < p.length; j2++) {
                    if (j1 == j2) {
                        continue;
                    }

                    int dx = Integer.compare(x[j2] - x[j1], 0);
                    vx[j1] += dx;
                }
            }
            for (int j = 0; j < p.length; j++) {
                x[j] += vx[j];
            }
            State state = new State(
                    x[0], x[1], x[2], x[3], vx[0], vx[1], vx[2], vx[3]
            );
            step++;
            if (!states.add(state)) {
                break;
            }
        }
        return step;
    }

    private static void part1(Vector3[] p, Vector3[] v, int stepCount) {
        for (int i = 0; i < stepCount; i++) {
            for (int j1 = 0; j1 < p.length; j1++) {
                for (int j2 = 0; j2 < p.length; j2++) {
                    if (j1 == j2) {
                        continue;
                    }
                    Vector3 p1 = p[j1];
                    Vector3 p2 = p[j2];
                    Vector3 p1v = v[j1];

                    int dx = Integer.compare(p2.x - p1.x, 0);
                    int dy = Integer.compare(p2.y - p1.y, 0);
                    int dz = Integer.compare(p2.z - p1.z, 0);
                    v[j1] = p1v.add(dx, dy, dz);
                }
            }
            for (int j = 0; j < p.length; j++) {
                p[j] = p[j].add(v[j]);
            }
            if (i + 1 == stepCount) {
                System.out.println("After " + (i + 1) + " steps:");
                for (int j = 0; j < p.length; j++) {
                    System.out.println("pos=" + p[j] + ", " + "vel=" + v[j]);
                }
            }

            if ((i + 1) == stepCount) {
                int e = 0;
                for (int j = 0; j < p.length; j++) {
                    e += (abs(p[j].x) + abs(p[j].y) + abs(p[j].z))
                            * (abs(v[j].x) + abs(v[j].y) + abs(v[j].z));
                }
                System.out.println("Sum of total energy: " + e);
            }
        }
    }

    private static final class State {
        final int x1, x2, x3, x4, v1, v2, v3, v4;

        private State(int x1, int x2, int x3, int x4, int v1, int v2, int v3, int v4) {
            this.x1 = x1;
            this.x2 = x2;
            this.x3 = x3;
            this.x4 = x4;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return x1 == state.x1 &&
                    x2 == state.x2 &&
                    x3 == state.x3 &&
                    x4 == state.x4 &&
                    v1 == state.v1 &&
                    v2 == state.v2 &&
                    v3 == state.v3 &&
                    v4 == state.v4;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x1, x2, x3, x4, v1, v2, v3, v4);
        }
    }
}
