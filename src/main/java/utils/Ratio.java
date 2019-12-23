package utils;

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

public class Ratio {
    public int rawNum, rawDen;
    public int num;   // the numerator
    public int numSign, denSign;
    public int den;   // the denominator

    // create and initialize a new Rational object
    public Ratio(int numerator, int denominator) {
        rawNum = numerator;
        rawDen = denominator;
        numSign = (int) Math.signum(numerator);
        denSign = (int) Math.signum(denominator);
        numerator = Math.abs(numerator);
        denominator = Math.abs(denominator);
        if (numerator == 0) {
            num = 0;
            den = 1;
            return;
        }
        if (denominator != 0) {
            int g = gcd(numerator, denominator);
            num = numerator / g;
            den = denominator / g;
        } else {
            num = 1;
            den = 0;
        }


    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ratio ratio = (Ratio) o;
        return num == ratio.num &&
                numSign == ratio.numSign &&
                denSign == ratio.denSign &&
                den == ratio.den;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num, numSign, denSign, den);
    }

    // return string representation of (this)
    public String toString() {
        return (num * numSign) + "/" + (denSign * den) + "(" + rawNum + "/" + rawDen + ")";
    }

    // return (this * b)
    public Ratio times(Ratio b) {
        return new Ratio(this.num * b.num * numSign * b.numSign, this.den * b.den & denSign * b.denSign);
    }


    // return (this + b)
    public Ratio plus(Ratio b) {
        int numerator = (this.num * numSign * b.den * denSign) + (this.den * denSign * b.num * b.numSign);
        int denominator = this.den * b.den * denSign * b.denSign;
        return new Ratio(numerator, denominator);
    }

    // return (1 / this)
    public Ratio reciprocal() {
        return new Ratio(den * denSign, num * numSign);
    }

    // return (this / b)
    public Ratio divides(Ratio b) {
        return this.times(b.reciprocal());
    }


    /***************************************************************************
     *  Helper functions
     ***************************************************************************/

    // return gcd(m, n)
    private static int gcd(int m, int n) {
        if (0 == n) return m;
        else return gcd(n, m % n);
    }

}