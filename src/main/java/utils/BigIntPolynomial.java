package utils;

import java.math.BigInteger;

public class BigIntPolynomial {
    public static final BigIntPolynomial X = new BigIntPolynomial(new BigInteger[]{BigInteger.ZERO, BigInteger.ONE});
    private final BigInteger[] factors;

    public BigIntPolynomial(BigInteger[] factors) {
        this.factors = factors;
    }

    public BigIntPolynomial addConst(BigInteger value) {
        BigInteger[] factors = this.factors.clone();
        factors[0] = factors[0].add(value);
        return new BigIntPolynomial(factors);
    }

    public BigIntPolynomial negate() {
        BigInteger[] factors = new BigInteger[this.factors.length];
        for (int i = 0; i < this.factors.length; i++) {
            factors[i] = this.factors[i].negate();
        }
        return new BigIntPolynomial(factors);
    }

    public BigIntPolynomial mulConst(BigInteger value) {
        BigInteger[] factors = new BigInteger[this.factors.length];
        for (int i = 0; i < this.factors.length; i++) {
            factors[i] = this.factors[i].multiply(value);
        }
        return new BigIntPolynomial(factors);
    }

    public BigIntPolynomial mod(BigInteger val) {
        BigInteger[] factors = new BigInteger[this.factors.length];
        for (int i = 0; i < this.factors.length; i++) {
            factors[i] = this.factors[i].remainder(val);
        }
        return new BigIntPolynomial(factors);
    }

    public BigInteger getFactor(int i) {
        return factors[i];
    }

    public BigInteger eval(BigInteger x) {
        BigInteger v = factors[0];
        BigInteger xPow = x;
        for (int i = 1; i < factors.length; i++) {
            v = v.add(factors[i].multiply(xPow));
            xPow = xPow.multiply(x);
        }
        return v;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = factors.length - 1; i >= 0; i--) {
            int deg = i;
            if (deg == 0) {
                sb.append(factors[i]);
            } else if (deg == 1) {
                sb.append(factors[i]).append("x");
            } else {
                sb.append(factors[i]).append("x^").append(deg);
            }
            if (i != 0) {
                sb.append(" + ");
            }
        }
        return sb.toString();
    }
}
