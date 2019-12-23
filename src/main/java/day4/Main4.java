package day4;

public class Main4 {
    public static void main(String[] args) {
        String in = "240298-784956";
        int from = Integer.parseInt(in.split("-")[0]);
        int to = Integer.parseInt(in.split("-")[1]);

        int count = 0, count2 = 0;
        for (int i = from; i <= to; i++) {
            if (verify(i)) {
                count++;
            }
            if (verify2(i)) {
                count2++;
            }
        }
        System.out.println("Part1 = " + count);
        System.out.println("Part2 = " + count2);
    }

    private static boolean verify(int i) {
        String s = i + "";
        char[] chars = s.toCharArray();
        boolean hasEqualPair = false;
        for (int j = 0; j < chars.length - 1; j++) {
            if (chars[j + 1] < chars[j]) {
                return false;
            }
            if (chars[j + 1] == chars[j]) {
                hasEqualPair = true;
            }
        }
        return hasEqualPair;
    }


    private static boolean verify2(int i) {
        String s = i + "";
        char[] chars = s.toCharArray();
        boolean hasEqualPair = false;
        for (int j = 0; j < chars.length - 1; j++) {
            if (chars[j + 1] < chars[j]) {
                return false;
            }
            boolean test = (j - 1 < 0 || (chars[j - 1] != chars[j]));
            if (chars[j + 1] == chars[j] &&
                    (j + 2 >= chars.length || chars[j + 2] != chars[j]) &&
                    test) {
                hasEqualPair = true;
            }
        }
        return hasEqualPair;
    }
}
