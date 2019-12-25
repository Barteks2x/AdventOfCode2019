import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public class Main24 {
    public static final long MASK = 0x007C7C7C7C7C0000L;
    public static final long MASK1 = 0xAA55AA55AA55AA55L;
    public static final long MASK2 = 0x55AA55AA55AA55AAL;

    public static final long CENTER = 0x0000001000000000L;
    public static final long CENTER_NEIGHBORS = left(CENTER, 1) | right(CENTER, 1) |
            up(CENTER, 1) | down(CENTER, 1);

    public static final long BOTTOM = 0x007C000000000000L;
    public static final long TOP = 0x00000000007C0000L;
    public static final long RIGHT = 0x0040404040400000L;
    public static final long LEFT = 0x0004040404040000L;

    public static final long BOTTOM_ONLY = BOTTOM & ~RIGHT & ~LEFT;
    public static final long TOP_ONLY = TOP & ~RIGHT & ~LEFT;
    public static final long RIGHT_ONLY = RIGHT & ~TOP & ~BOTTOM;
    public static final long LEFT_ONLY = LEFT & ~TOP & ~BOTTOM;

    public static final long RECURSIVE_NORMAL = MASK & ~CENTER & ~CENTER_NEIGHBORS;
    public static final long RECURSIVE_NORMAL_MASK1 = MASK1 & ~CENTER;
    public static final long RECURSIVE_NORMAL_MASK2 = MASK2 & ~CENTER;

    public static void main(String[] args) {
        String in = "" +
                "##.#.\n" +
                "#..#.\n" +
                ".....\n" +
                "....#\n" +
                "#.###";

        long layout = parse(in);
        long startLayout = layout;

        LongOpenHashSet set = new LongOpenHashSet(4096);
        while (!set.contains(layout)) {
            set.add(layout);
            layout = computeNext(layout);
        }
        System.out.println("Layout after loop: \n" + toString(layout));
        System.out.println("Biodiversity after loop: " + packToInt(layout));

        long[] recursiveMap = new long[500];
        final int l0 = 250;
        recursiveMap[l0] = startLayout;

        for (int i = 0; i < 200; i++) {
            recursiveMap = computeNextRecursive(recursiveMap, l0);
        }
        int count = 0;
        for (long l : recursiveMap) {
            count += Long.bitCount(l);
        }
        System.out.println("Count after 200 recursive iterations: " + count);
    }

    private static long packToInt(long start) {
        long total = 0;
        start >>>= 18;
        total |= start & 0x1F;
        start >>>= 8;
        total |= (start & 0x1F) << 5;
        start >>>= 8;
        total |= (start & 0x1F) << 10;
        start >>>= 8;
        total |= (start & 0x1F) << 15;
        start >>>= 8;
        total |= (start & 0x1F) << 20;
        return total;
    }

    private static long parse(String in) {
        long out = 0;
        String[] arr = in.split("\n");
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                int off = i + 2 + (j + 2) * 8;
                if (arr[j].charAt(i) == '#') {
                    out |= (1L << off);
                }
            }
        }
        return out;
    }

    private static String toString(long x) {
        StringBuilder sb = new StringBuilder(6 * 5);
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 5; i++) {
                int off = i + 2 + (j + 2) * 8;
                sb.append((x & (1L << off)) == 0 ? '.' : '#');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static long computeNext(long start) {
        return (applyRulesWithMask(start, MASK1) | applyRulesWithMask(start, MASK2)) & MASK;
    }

    // requires that the positions for which the rules should be applied are NOT
    // in the mask, but all of their neighbors are
    private static long applyRulesWithMask(long layout, long mask) {
        long n = layout & mask;

        long a = left(n, 1);
        long b = right(n, 1);
        long c = up(n, 1);
        long d = down(n, 1);

        long abx = a ^ b;
        long cdx = c ^ d;
        long ab = a & b;
        long cd = c & d;

        long out = 0;
        out |= (abx ^ cdx) & ~((a & b) | (c & d));
        out |= (~layout) & (
                (abx & cdx) | (ab & ~(c | d)) | (cd & ~(a | b))
        );
        return out;
    }


    private static long[] computeNextRecursive(long[] start, int l0) {
        long[] outMap = new long[start.length];
        for (int i = 1; i < start.length - 1; i++) {
            // +1 --> smaller (deeper)
            // -1 --> bigger
            long lSmall = start[i + 1];
            long level = start[i];
            long lBig = start[i - 1];
            if (lSmall == 0 && level == 0 && lBig == 0) {
                continue;
            }

            long bigLeft = lBig & left(CENTER, 1);
            long bigRight = lBig & right(CENTER, 1);
            long bigUp = lBig & up(CENTER, 1);
            long bigDown = lBig & down(CENTER, 1);

            long smallLeft = lSmall & LEFT;
            long smallRight = lSmall & RIGHT;
            long smallUp = lSmall & TOP;
            long smallDown = lSmall & BOTTOM;

            if (bigLeft != 0) {
                level |= left(LEFT, 1);
            }
            if (bigRight != 0) {
                level |= right(RIGHT, 1);
            }
            if (bigUp != 0) {
                level |= up(TOP, 1);
            }
            if (bigDown != 0) {
                level |= down(BOTTOM, 1);
            }

            long out = (applyRulesWithMask(level, RECURSIVE_NORMAL_MASK1) | applyRulesWithMask(level, RECURSIVE_NORMAL_MASK2)) & RECURSIVE_NORMAL;

            int bitcountUp = Long.bitCount(smallUp) + Long.bitCount(level & up(CENTER_NEIGHBORS, 1) & ~CENTER);
            int bitcountDown = Long.bitCount(smallDown) + Long.bitCount(level & down(CENTER_NEIGHBORS, 1) & ~CENTER);
            int bitcountLeft = Long.bitCount(smallLeft) + Long.bitCount(level & left(CENTER_NEIGHBORS, 1) & ~CENTER);
            int bitcountRight = Long.bitCount(smallRight) + Long.bitCount(level & right(CENTER_NEIGHBORS, 1) & ~CENTER);

            if(bitcountUp == 1 || (bitcountUp == 2 && (level & up(CENTER, 1)) == 0)) {
                out |= up(CENTER, 1);
            }
            if(bitcountDown == 1 || (bitcountDown == 2 && (level & down(CENTER, 1)) == 0)) {
                out |= down(CENTER, 1);
            }
            if(bitcountLeft == 1 || (bitcountLeft == 2 && (level & left(CENTER, 1)) == 0)) {
                out |= left(CENTER, 1);
            }
            if(bitcountRight == 1 || (bitcountRight == 2 && (level & right(CENTER, 1)) == 0)) {
                out |= right(CENTER, 1);
            }
            outMap[i] = out & MASK;
        }
        return outMap;
    }

    private static long left(long in, int amount) {
        return in >>> amount;
    }

    private static long right(long in, int amount) {
        return in << amount;
    }

    private static long down(long in, int amount) {
        return in << amount * 8;
    }

    private static long up(long in, int amount) {
        return in >>> amount * 8;
    }

}
