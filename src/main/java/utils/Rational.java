package utils;

import java.math.BigInteger;
import java.util.Objects;

/******************************************************************************
 *  Compilation:  javac Rational.java
 *  Execution:    java Rational
 *
 *  ADT for nonnegative Rational numbers. Bare-bones implementation.
 *  Cancel common factors, but does not stave off overflow. Does not
 *  support negative fractions.
 *
 *  Invariant: all Rational objects are in reduced form (except
 *  possibly while modifying).
 *
 *  Remarks
 *  --------
 *    - See https://introcs.cs.princeton.edu/java/92symbolic/BigRational.java.html
 *      for a version that supports negative fractions and arbitrary
 *      precision numerators and denominators.
 *
 *  % java Rational
 *  5/6
 *  1
 *  28/51
 *  17/899
 *  0
 *
 ******************************************************************************/

public class Rational {
    private BigInteger num;   // the numerator
    private BigInteger den;   // the denominator

    // create and initialize a new Rational object
    public Rational(BigInteger numerator, BigInteger denominator) {
        if (denominator.equals(BigInteger.ZERO)) {
            throw new RuntimeException("Denominator is zero");
        }
        BigInteger g = gcd(numerator, denominator);
        num = numerator.divide(g);
        den = denominator.divide(g);
    }

    public Rational(int numerator, int denominator) {
        this(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    // return string representation of (this)
    public String toString() {
        if (den.equals(BigInteger.ONE)) return num + "";
        else return num + "/" + den;
    }

    // return (this * b)
    public Rational times(Rational b) {
        return new Rational(this.num.multiply(b.num), this.den.multiply(b.den));
    }


    // return (this + b)
    public Rational plus(Rational b) {
        BigInteger numerator = (this.num.multiply(b.den)).add(this.den.multiply(b.num));
        BigInteger denominator = this.den.multiply(b.den);
        return new Rational(numerator, denominator);
    }

    // return (1 / this)
    public Rational reciprocal() {
        return new Rational(den, num);
    }

    // return (this / b)
    public Rational divides(Rational b) {
        return this.times(b.reciprocal());
    }


    /***************************************************************************
     *  Helper functions
     ***************************************************************************/

    // return gcd(m, n)
    private static BigInteger gcd(BigInteger m, BigInteger n) {
        if (n.equals(BigInteger.ZERO)) return m;
        else return gcd(n, m.mod(n));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rational rational = (Rational) o;
        return num.equals(rational.num) &&
                den.equals(rational.den);
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, den);
    }

    public boolean lessThan(Rational other) {
        return this.num.multiply(other.den).compareTo(other.num.multiply(this.den)) < 0;
    }
}



