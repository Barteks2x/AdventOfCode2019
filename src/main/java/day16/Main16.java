package day16;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main16 {
    public static void main(String[] args) throws IOException {
        String number = Files.readAllLines(Paths.get("run/in16.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .reduce((a, b) -> a + b)
                .orElse("");
        char[] digits = number.toCharArray();
        int[] nums = new int[digits.length];
        for (int i = 0; i < digits.length; i++) {
            nums[i] = digits[i] - '0';
        }
        int[] newNums = nums;
        for (int i = 0; i < 100; i++) {
            newNums = applyPhase(newNums);
        }
        System.out.println(Arrays.toString(newNums));

        int[] signal = new int[nums.length * 10000];
        for (int i = 0; i < 10000; i++) {
            System.arraycopy(nums, 0, signal, i * nums.length, nums.length);
        }
        int offset = signal[6] + signal[5] * 10 + signal[4] * 100 + signal[3] * 1000 + signal[2] * 10000 + signal[1] * 100000 + signal[0] * 1000000;
        int[] newSignal = signal;
        for (int i = 0; i < 100; i++) {
            newSignal = applyPhase(newSignal);
            System.out.print("Phase " + (i + 1) + ": ");
            for (int j = 0; j < 8; j++) {
                System.out.print(newSignal[j + offset]);
            }
            System.out.println();
        }
    }

    private static int[] applyPhase(int[] nums) {
        int[] sumUpTo = new int[nums.length + 1];
        int s = 0;
        for (int i = 0; i < sumUpTo.length; i++) {
            sumUpTo[i] = s;
            if (i < nums.length) {
                s += nums[i];
            }
        }
        int[] newNums = new int[nums.length];
        for (int i = 0; i < newNums.length; i++) {
            int sum = 0;
            int d = i + 1;
            d--;// offset by one
            int j = 0;
            while (j < nums.length) {
                j += d; // zeros
                if (j > nums.length) break;
                d = i + 1;
                int min = j;
                int max = Math.min(j + d, nums.length);
                sum += sumUpTo[max] - sumUpTo[min];
                j += d; // 1
                j += d; // zeros
                if (j > nums.length) break;
                min = j;
                max = Math.min(j + d, nums.length);
                sum -= sumUpTo[max] - sumUpTo[min];
                j += d; // -1
            }
            newNums[i] = Math.abs(sum % 10);
        }
        return newNums;
    }
}
