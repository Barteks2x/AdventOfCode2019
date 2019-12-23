package day22;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import utils.BigIntPolynomial;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main22Old {
    public static void main(String[] args) throws IOException {
        String[] data = Files.readAllLines(Paths.get("run/in22.txt")).stream()
                .filter(x -> !x.isEmpty() && !x.startsWith("#"))
                .toArray(String[]::new);

        int[] cards = new int[10007];
        int[] buffer = new int[cards.length];
        for (int i = 0; i < cards.length; i++) {
            cards[i] = i;
        }
        for (String instr : data) {
            String[] split = instr.split("\\s");
            int[] newCards;
            if (split[0].equals("cut")) {
                newCards = cut(cards, buffer, Integer.parseInt(split[1]));
            } else if (split[0].equals("deal")) {
                if (split[1].equals("into")) {
                    newCards = deal(cards, buffer);
                } else if (split[1].equals("with")) {
                    int incr = Integer.parseInt(split[3]);
                    newCards = dealWithIncr(cards, buffer, incr);
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException(instr);
            }
            verify(newCards, instr);
            buffer = cards;
            cards = newCards;
        }
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == 2019) {
                System.out.println(i);
            }
        }

       /* BigInteger initLen = BigInteger.valueOf(cards.length);
        long[] insnsInit = makeInsns(data, cards.length);
        BigPolynomial polyInit = createPolynomial(insnsInit, initLen).mod(initLen).addConst(initLen).mod(initLen);
        System.out.println(polyInit);
        BigInteger b1 = polyInit.getFactor(0);
        BigInteger a1 = polyInit.getFactor(1);
        BigInteger prevInit = modDivide(BigInteger.valueOf(5755).subtract(b1), a1, initLen);
        System.out.println(prevInit);
        System.out.println(polyInit.eval(BigInteger.valueOf(2019)).mod(initLen));
        System.out.println("Next=" + nextPosOf(insnsInit, initLen, BigInteger.valueOf(2019)).mod(initLen));
        System.out.println("Prev=" + prevPosOf(insnsInit, initLen, BigInteger.valueOf(5755)).mod(initLen));


        System.out.println(posOf(a1, b1, BigInteger.valueOf(5755),
                BigInteger.valueOf(2), initLen
                ) + " p2=" + prevPosOf(insnsInit, initLen, BigInteger.valueOf(2019)).mod(initLen) );
        if (true) return;*/


        long applyCount = 101_741_582_076_661L;
        long len = 119_315_717_514_047L;
        BigInteger pos = BigInteger.valueOf(5755);
        long[] insns = makeInsns(data, len);
        BigInteger blen = BigInteger.valueOf(len);

        BigIntPolynomial polynomial = createPolynomial(insns, blen).mod(blen).addConst(blen).mod(blen);

        // y = ax + b
        BigInteger b = polynomial.getFactor(0);
        BigInteger a = polynomial.getFactor(1);

        System.out.println(posOf(a, b, BigInteger.valueOf(2020),
                BigInteger.valueOf(applyCount), blen
        ));

        if(true) return;
        // y - b = ax
        // x = (y - b) / a
        System.out.println(polynomial);
        BigInteger prev = null;
        for (long j = 0; j < applyCount; j++) {
            BigInteger peval = modDivide(pos.subtract(b), a, blen);
            //System.out.println(peval);
            pos = peval;
            if (j % 1000000000L == 0) {
                System.out.println(j);
            }
        }

    }

    private static BigInteger posOf(BigInteger a, BigInteger b, BigInteger value, BigInteger shuffleCount, BigInteger len) {
        BigInteger ak = a.modPow(shuffleCount, len);
        BigInteger seriesSum = modDivide(ak.subtract(BigInteger.ONE), a.subtract(BigInteger.ONE), len);
        BigInteger val1 = value.subtract(b.multiply(seriesSum)).mod(len);
        BigInteger result = modDivide(val1, ak, len);
        return result;
    }

    // Function to compute a/b under modlo m
    static BigInteger modDivide(BigInteger a, BigInteger b, BigInteger m) {
        a = a.mod(m);
        BigInteger inv = b.modInverse(m);
        return inv.multiply(a).mod(m);
    }

    private static long[] makeInsns(String[] data, long len) {
        long[] insns = new long[data.length * 2];
        int idx = 0;
        for (int i = data.length - 1; i >= 0; i--, idx += 2) {
            String[] split = data[i].split("\\s");
            if (split[0].equals("cut")) {
                long cut = Integer.parseInt(split[1]);
                if (cut < 0) {
                    cut += len;
                }
                insns[idx] = 0;
                insns[idx + 1] = cut;
            } else if (split[0].equals("deal")) {
                if (split[1].equals("into")) {
                    insns[idx] = 1;
                } else if (split[1].equals("with")) {
                    insns[idx] = 2;
                    insns[idx + 1] = Long.parseLong(split[3]);
                } else {
                    throw new RuntimeException();
                }
            } else {
                throw new RuntimeException(data[i]);
            }
        }
        return insns;
    }

    private static BigInteger nextPosOf(long[] data, BigInteger len, BigInteger pos) {
        for (int i = data.length - 2; i >= 0; i -= 2) {
            if (data[i] == 0) {
                pos = pos.subtract(BigInteger.valueOf(data[i + 1]));
            } else if (data[i] == 1) {
                BigInteger cpos = pos.remainder(len);
                BigInteger npos = len.subtract(BigInteger.ONE).subtract(cpos).add(len);
                pos = pos.add(npos.subtract(cpos));
            } else if (data[i] == 2) {
                BigInteger incr = BigInteger.valueOf(data[i + 1]);
                pos = pos.multiply(incr);
            } else {
                throw new RuntimeException();
            }
        }
        return pos;
    }


    private static BigIntPolynomial createPolynomial(long[] data, BigInteger len) {
        BigIntPolynomial poly = BigIntPolynomial.X;
        for (int i = data.length - 2; i >= 0; i -= 2) {
            if (data[i] == 0) {
                poly = poly.addConst(BigInteger.valueOf(-data[i + 1]));
            } else if (data[i] == 1) {
                poly = poly.negate().addConst(len.subtract(BigInteger.ONE));
            } else if (data[i] == 2) {
                BigInteger incr = BigInteger.valueOf(data[i + 1]);
                poly = poly.mulConst(incr);
            } else {
                throw new RuntimeException();
            }
        }
        return poly;
    }

    private static BigInteger prevPosOf(long[] data, BigInteger len, BigInteger pos) {

        for (int i = 0; i < data.length; i += 2) {
            if (data[i] == 0) {
                pos = pos.add(BigInteger.valueOf(data[i + 1]));
                // if (pos >= len) {
                //     pos -= len;
                // }
            } else if (data[i] == 1) {
                BigInteger cpos = pos.remainder(len);
                BigInteger npos = len.subtract(BigInteger.ONE).subtract(cpos);
                if (npos.subtract(cpos).compareTo(BigInteger.ZERO) < 0)
                    npos = npos.add(len);
                pos = pos.add(npos.subtract(cpos));
            } else if (data[i] == 2) {
                BigInteger cpos = pos.remainder(len);

                BigInteger npos = cpos;
                BigInteger incr = BigInteger.valueOf(data[i + 1]);
                while (!npos.remainder(incr).equals(BigInteger.ZERO)) {
                    npos = npos.add(len);
                }
                npos = npos.divide(incr);
                if (npos.subtract(cpos).compareTo(BigInteger.ZERO) < 0)
                    npos = npos.add(len);

                pos = pos.add(npos.subtract(cpos));
            } else {
                throw new RuntimeException();
            }
        }
        return pos;
    }

    private static long gcd(long number1, long number2) {
        //base case
        if (number2 == 0) {
            return number1;
        }
        return gcd(number2, number1 % number2);
    }

    private static void verify(int[] newCards, String instr) {
        IntSet set = new IntOpenHashSet(newCards.length);
        for (int newCard : newCards) {
            set.add(newCard);
        }
        if (set.size() != newCards.length) {
            throw new RuntimeException("missing " + (newCards.length - set.size()) + ", " + instr);
        }
    }

    private static int[] dealWithIncr(int[] cards, int[] buffer, int incr) {
        int len = cards.length;
        for (int i = 0, j = 0; i < len; i++, j += incr) {
            while (j >= len) {
                j -= len;
            }
            buffer[j] = cards[i];
        }
        return buffer;
    }

    private static int[] deal(int[] cards, int[] buffer) {
        int len = cards.length;
        for (int i = 0, j = len - 1; i < len && j >= 0; i++, j--) {
            buffer[i] = cards[j];
        }
        return buffer;
    }

    private static int[] cut(int[] cards, int[] buffer, int position) {
        if (position < 0) {
            position += cards.length;
        }
        int end = position;
        int i = 0;
        do {
            buffer[i] = cards[position];
            position++;
            if (position >= cards.length) {
                position = 0;
            }
            i++;
        } while (position != end);
        return buffer;
    }
}
