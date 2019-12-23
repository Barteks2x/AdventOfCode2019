package day22;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main22Clean {
    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        String[] data = Files.readAllLines(Paths.get("run/in22.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#")).toArray(String[]::new);

        BigInteger lenPart1 = BigInteger.valueOf(10007);
        BigInteger[] func = createFunction(data, lenPart1);
        BigInteger a = func[1];
        BigInteger b = func[0];
        System.out.println("Part1 = " + a.multiply(BigInteger.valueOf(2019)).add(b).mod(lenPart1).add(lenPart1).mod(lenPart1));

        BigInteger shuffles = BigInteger.valueOf(101741582076661L);
        BigInteger lenPart2 = BigInteger.valueOf(119315717514047L);
        BigInteger[] func2 = createFunction(data, lenPart2);
        BigInteger a2 = func2[1];
        BigInteger b2 = func2[0];
        BigInteger value = BigInteger.valueOf(2020);
        System.out.println("Part2 = " + posOf(a2, b2, value, shuffles, lenPart2));
        System.out.println("Dt    = " + (System.currentTimeMillis() - time) * 0.001);
    }

    private static BigInteger posOf(BigInteger a, BigInteger b, BigInteger value, BigInteger shuffleCount, BigInteger len) {
        BigInteger ak = a.modPow(shuffleCount, len);
        BigInteger seriesSum = ak.subtract(BigInteger.ONE).multiply(a.subtract(BigInteger.ONE).modInverse(len));
        BigInteger val1 = value.subtract(b.multiply(seriesSum)).mod(len);
        return val1.multiply(ak.modInverse(len)).mod(len);
    }

    private static BigInteger[] createFunction(String[] data, BigInteger len) {
        BigInteger[] func = new BigInteger[]{
                BigInteger.ZERO, // * x^0
                BigInteger.ONE   // * x^1
        };
        for (String instr : data) {
            String[] split = instr.split("\\s");
            if (split[0].equals("cut")) {
                func[0] = func[0].subtract(new BigInteger(split[1]));
            } else if (split[0].equals("deal")) {
                if (split[1].equals("into")) {
                    func[0] = func[0].negate().add(len.subtract(BigInteger.ONE));
                    func[1] = func[1].negate();
                } else if (split[1].equals("with")) {
                    BigInteger incr = new BigInteger(split[3]);
                    func[0] = func[0].multiply(incr);
                    func[1] = func[1].multiply(incr);
                } else throw new RuntimeException(instr);
            } else throw new RuntimeException(instr);
        }
        return func;
    }
}
